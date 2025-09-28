package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.dto.SaleDto;
import com.example.multi_tanent.pos.dto.SaleRequest;
import com.example.multi_tanent.pos.dto.SaleItemDto;
import com.example.multi_tanent.pos.entity.*;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.enitity.User; // Corrected import
import com.example.multi_tanent.spersusers.repository.UserRepository; // Corrected import
import com.example.multi_tanent.pos.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import java.util.UUID;

@Service
@Transactional("tenantTx")
public class SaleService {

    private final SaleRepository saleRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductVariantRepository productVariantRepository;

    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final StockMovementService stockMovementService;
    
    public SaleService(SaleRepository saleRepository, TenantRepository tenantRepository,
                       UserRepository userRepository, CustomerRepository customerRepository,
                       ProductVariantRepository productVariantRepository, StoreRepository storeRepository,
                       InventoryRepository inventoryRepository, StockMovementService stockMovementService) {
        this.saleRepository = saleRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.productVariantRepository = productVariantRepository;
        this.stockMovementService = stockMovementService;

        this.inventoryRepository = inventoryRepository;
        this.storeRepository = storeRepository;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found."));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in current tenant."));
    }

    public SaleDto createSale(SaleRequest request) {
        Tenant currentTenant = getCurrentTenant();
        User currentUser = getCurrentUser();

        Sale sale = new Sale();
        sale.setTenant(currentTenant);
        sale.setUser(currentUser);
        sale.setInvoiceNo(generateInvoiceNumber());
        sale.setInvoiceDate(OffsetDateTime.now());
        sale.setStatus("completed"); // Default status

        // Associate store if provided or from user
        // storeId is now mandatory in SaleRequest
        Store saleStore = storeRepository.findByIdAndTenantId(request.getStoreId(), currentTenant.getId())
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + request.getStoreId()));
        sale.setStore(saleStore);

        // Associate customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findByIdAndTenantId(request.getCustomerId(), currentTenant.getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));
            sale.setCustomer(customer);
        }

        long subtotal = 0L;
        long totalTax = 0L;

        List<SaleItem> saleItems = request.getItems().stream().map(itemRequest -> {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("ProductVariant not found with id: " + itemRequest.getProductVariantId()));

            if (!variant.getProduct().getTenant().getId().equals(currentTenant.getId())) {
                throw new SecurityException("Attempted to sell a product from another tenant.");
            }

            long lineTotal = variant.getPriceCents() * itemRequest.getQuantity();
            long lineTax = 0L;
            if (variant.getTaxRate() != null) {
                lineTax = (lineTotal * variant.getTaxRate().getPercent().longValue()) / 100;
            }

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProductVariant(variant);
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPriceCents(variant.getPriceCents());
            saleItem.setLineTotalCents(lineTotal);
            saleItem.setTaxCents(lineTax);
            saleItem.setDiscountCents(0L);
            return saleItem;
        }).collect(Collectors.toList());

        for (SaleItem item : saleItems) {
            subtotal += item.getLineTotalCents();
            totalTax += item.getTaxCents();
        }

        sale.setItems(saleItems);
        sale.setSubtotalCents(subtotal);
        sale.setTaxCents(totalTax);
        sale.setDiscountCents(request.getDiscountCents() != null ? request.getDiscountCents() : 0L);
        sale.setTotalCents(subtotal + totalTax - sale.getDiscountCents());

        long totalPaid = 0L;
        if (request.getPayments() != null && !request.getPayments().isEmpty()) {
            List<Payment> payments = request.getPayments().stream().map(paymentRequest -> {
                Payment payment = new Payment();
                payment.setSale(sale);
                payment.setMethod(paymentRequest.getMethod());
                payment.setAmountCents(paymentRequest.getAmountCents());
                payment.setReference(paymentRequest.getReference());
                return payment;
            }).collect(Collectors.toList());
            sale.setPayments(payments);
            totalPaid = payments.stream().mapToLong(Payment::getAmountCents).sum();
        }

        if (totalPaid >= sale.getTotalCents()) {
            sale.setPaymentStatus("paid");
        } else if (totalPaid > 0) {
            sale.setPaymentStatus("partial");
        } else {
            sale.setPaymentStatus("unpaid");
        }

        Sale savedSale = saleRepository.save(sale);

        // After sale is saved, create stock movements which will update inventory
        savedSale.getItems().forEach(item -> {
            stockMovementService.createMovementForSale(
                    savedSale,
                    item.getProductVariant(),
                    -item.getQuantity() // Negative quantity for a sale
            );
        });

        return toDto(savedSale);
    }

    @Transactional(readOnly = true)
    public List<SaleDto> getAllSalesForCurrentTenant() {
        Tenant currentTenant = getCurrentTenant();
        return saleRepository.findByTenantId(currentTenant.getId()).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Sale> getSaleById(Long id) {
        Tenant currentTenant = getCurrentTenant();
        return saleRepository.findByIdAndTenantId(id, currentTenant.getId());
    }

    @Transactional(readOnly = true)
    public Optional<SaleDto> getSaleDtoById(Long id) {
        return getSaleById(id).map(this::toDto);
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private SaleDto toDto(Sale sale) {
        SaleDto dto = new SaleDto();
        dto.setId(sale.getId());
        dto.setInvoiceNo(sale.getInvoiceNo());
        dto.setInvoiceDate(sale.getInvoiceDate());
        dto.setStatus(sale.getStatus());
        dto.setPaymentStatus(sale.getPaymentStatus());

        if (sale.getCustomer() != null) {
            dto.setCustomerId(sale.getCustomer().getId());
            dto.setCustomerName(sale.getCustomer().getName());
        }
        if (sale.getStore() != null) {
            dto.setStoreId(sale.getStore().getId());
            dto.setStoreName(sale.getStore().getName());
        }

        dto.setSubtotalCents(sale.getSubtotalCents());
        dto.setTaxCents(sale.getTaxCents());
        dto.setDiscountCents(sale.getDiscountCents());
        dto.setTotalCents(sale.getTotalCents());

        dto.setItems(sale.getItems().stream().map(this::toSaleItemDto).collect(Collectors.toList()));
        return dto;
    }

    private SaleItemDto toSaleItemDto(SaleItem saleItem) {
        SaleItemDto dto = new SaleItemDto();
        dto.setId(saleItem.getId());
        if (saleItem.getProductVariant() != null) {
            dto.setProductVariantId(saleItem.getProductVariant().getId());
            if (saleItem.getProductVariant().getProduct() != null) {
                dto.setProductName(saleItem.getProductVariant().getProduct().getName());
            }
        }
        dto.setQuantity(saleItem.getQuantity());
        dto.setUnitPriceCents(saleItem.getUnitPriceCents());
        dto.setLineTotalCents(saleItem.getLineTotalCents());
        dto.setTaxCents(saleItem.getTaxCents());
        dto.setDiscountCents(saleItem.getDiscountCents());
        return dto;
    }
}