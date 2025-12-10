package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.entity.CrmSalesProduct;
import com.example.multi_tanent.crm.repository.CrmSalesProductRepository;
import com.example.multi_tanent.sales.dto.*;
import com.example.multi_tanent.sales.entity.SalesInvoice;
import com.example.multi_tanent.sales.entity.SalesInvoiceItem;
import com.example.multi_tanent.sales.repository.SalesInvoiceRepository;
import com.example.multi_tanent.spersusers.enitity.BaseCustomer;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.spersusers.repository.PartyRepository;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import com.example.multi_tanent.tenant.service.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesInvoiceService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final TenantRepository tenantRepository;
    private final PartyRepository partyRepository;
    private final EmployeeRepository employeeRepository;
    private final CrmSalesProductRepository crmSalesProductRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public SalesInvoiceResponse createSalesInvoice(SalesInvoiceRequest request, List<MultipartFile> files) {
        String tenantIdentifier = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantId(tenantIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setTenant(tenant);

        if (request.getCustomerId() != null) {
            BaseCustomer customer = partyRepository.findByTenantIdAndId(tenant.getId(), request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            invoice.setCustomer(customer);
        }

        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        mapRequestToEntity(request, invoice, tenant.getId());

        if (files != null && !files.isEmpty()) {
            List<String> attachmentUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file, "sales_invoices", false);
                    attachmentUrls.add(fileName);
                }
            }
            invoice.setAttachments(attachmentUrls);
        }

        calculateTotals(invoice);

        invoice = salesInvoiceRepository.save(invoice);
        return mapEntityToResponse(invoice);
    }

    @Transactional
    public SalesInvoiceResponse updateSalesInvoice(Long id, SalesInvoiceRequest request, List<MultipartFile> files) {
        String tenantIdentifier = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantId(tenantIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        SalesInvoice invoice = salesInvoiceRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sales Invoice not found"));

        if (request.getCustomerId() != null) {
            BaseCustomer customer = partyRepository.findByTenantIdAndId(tenant.getId(), request.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            invoice.setCustomer(customer);
        }

        mapRequestToEntity(request, invoice, tenant.getId());

        if (files != null && !files.isEmpty()) {
            if (invoice.getAttachments() == null) {
                invoice.setAttachments(new ArrayList<>());
            }
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file, "sales_invoices", false);
                    invoice.getAttachments().add(fileName);
                }
            }
        }

        calculateTotals(invoice);

        invoice = salesInvoiceRepository.save(invoice);
        return mapEntityToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public SalesInvoiceResponse getSalesInvoiceById(Long id) {
        String tenantIdentifier = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantId(tenantIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        SalesInvoice invoice = salesInvoiceRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sales Invoice not found"));
        return mapEntityToResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Page<SalesInvoiceResponse> getAllSalesInvoices(String search, LocalDate fromDate, LocalDate toDate,
            Long salespersonId, Pageable pageable) {
        String tenantIdentifier = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantId(tenantIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        Page<SalesInvoice> invoices = salesInvoiceRepository.searchSalesInvoices(tenant.getId(), search,
                fromDate, toDate, salespersonId, pageable);
        return invoices.map(this::mapEntityToResponse);
    }

    @Transactional
    public void deleteSalesInvoice(Long id) {
        String tenantIdentifier = TenantContext.getTenantId();
        Tenant tenant = tenantRepository.findByTenantId(tenantIdentifier)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));

        SalesInvoice invoice = salesInvoiceRepository.findByIdAndTenantId(id, tenant.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sales Invoice not found"));
        salesInvoiceRepository.delete(invoice);
    }

    // Placeholder for PDF Generation
    public byte[] generatePdf(Long id) {
        return new byte[0]; // To be implemented
    }

    private void mapRequestToEntity(SalesInvoiceRequest request, SalesInvoice invoice, Long tenantId) {
        if (request.getInvoiceLedger() != null)
            invoice.setInvoiceLedger(request.getInvoiceLedger());
        if (request.getInvoiceDate() != null)
            invoice.setInvoiceDate(request.getInvoiceDate());
        if (request.getReference() != null)
            invoice.setReference(request.getReference());
        if (request.getOrderNumber() != null)
            invoice.setOrderNumber(request.getOrderNumber());
        if (request.getDueDate() != null)
            invoice.setDueDate(request.getDueDate());
        if (request.getDateOfSupply() != null)
            invoice.setDateOfSupply(request.getDateOfSupply());

        // New Fields
        if (request.getEnableGrossNetWeight() != null)
            invoice.setEnableGrossNetWeight(request.getEnableGrossNetWeight());
        if (request.getDelayReason() != null)
            invoice.setDelayReason(request.getDelayReason());
        if (request.getAmountReceived() != null)
            invoice.setAmountReceived(request.getAmountReceived());
        if (request.getBalanceDue() != null)
            invoice.setBalanceDue(request.getBalanceDue());

        if (request.getSubTotal() != null)
            invoice.setSubTotal(request.getSubTotal());
        if (request.getTotalDiscount() != null)
            invoice.setTotalDiscount(request.getTotalDiscount());
        if (request.getOtherCharges() != null)
            invoice.setOtherCharges(request.getOtherCharges());

        if (request.getTermsAndConditions() != null)
            invoice.setTermsAndConditions(request.getTermsAndConditions());
        if (request.getNotes() != null)
            invoice.setNotes(request.getNotes());
        if (request.getTemplate() != null)
            invoice.setTemplate(request.getTemplate());
        if (request.getEmailTo() != null)
            invoice.setEmailTo(request.getEmailTo());
        if (request.getStatus() != null)
            invoice.setStatus(request.getStatus());

        if (request.getSalespersonId() != null) {
            Employee salesperson = employeeRepository.findById(request.getSalespersonId()).orElse(null);
            invoice.setSalesperson(salesperson);
        }

        if (request.getItems() != null) {
            if (invoice.getItems() != null) {
                invoice.getItems().clear();
            } else {
                invoice.setItems(new ArrayList<>());
            }

            for (SalesInvoiceItemRequest itemRequest : request.getItems()) {
                SalesInvoiceItem item = new SalesInvoiceItem();
                item.setSalesInvoice(invoice);

                if (itemRequest.getCrmProductId() != null) {
                    CrmSalesProduct product = crmSalesProductRepository
                            .findByIdAndTenantId(itemRequest.getCrmProductId(), tenantId)
                            .orElseThrow(() -> new EntityNotFoundException(
                                    "Product not found: " + itemRequest.getCrmProductId()));
                    item.setCrmProduct(product);
                    item.setItemCode(
                            itemRequest.getItemCode() != null ? itemRequest.getItemCode() : product.getItemCode());
                    item.setItemName(itemRequest.getItemName() != null ? itemRequest.getItemName() : product.getName());
                } else {
                    item.setItemCode(itemRequest.getItemCode());
                    item.setItemName(itemRequest.getItemName());
                }

                item.setDescription(itemRequest.getDescription());
                item.setPackingType(itemRequest.getPackingType());

                // Specific Quantities
                item.setQuantityGross(itemRequest.getQuantityGross());
                item.setQuantityNet(itemRequest.getQuantityNet());
                item.setSendQuantity(itemRequest.getSendQuantity());
                item.setInvoiceQuantity(itemRequest.getInvoiceQuantity());

                item.setRate(itemRequest.getRate());
                item.setTaxValue(itemRequest.getTaxValue());
                item.setTaxPercentage(itemRequest.getTaxPercentage());
                item.setTaxExempt(itemRequest.isTaxExempt());

                BigDecimal amount = BigDecimal.ZERO;
                // Prefer invoice quantity for calculation if present, else send, else net?
                // Assumption: invoiceQuantity is the billing quantity
                BigDecimal qty = item.getInvoiceQuantity() != null ? item.getInvoiceQuantity() : BigDecimal.ZERO;

                if (item.getRate() != null) {
                    amount = item.getRate().multiply(qty);
                }
                item.setAmount(amount);

                invoice.getItems().add(item);
            }
        }
    }

    private void calculateTotals(SalesInvoice invoice) {
        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;

        if (invoice.getItems() != null) {
            for (SalesInvoiceItem item : invoice.getItems()) {
                if (item.getAmount() != null) {
                    subTotal = subTotal.add(item.getAmount());
                }
                if (!item.isTaxExempt() && item.getTaxValue() != null) {
                    totalTax = totalTax.add(item.getTaxValue());
                }
            }
        }

        invoice.setSubTotal(subTotal);

        BigDecimal discount = invoice.getTotalDiscount() != null ? invoice.getTotalDiscount() : BigDecimal.ZERO;

        BigDecimal grossTotal = subTotal.subtract(discount);
        invoice.setGrossTotal(grossTotal);

        invoice.setTotalTax(totalTax);

        BigDecimal otherCharges = invoice.getOtherCharges() != null ? invoice.getOtherCharges() : BigDecimal.ZERO;

        invoice.setNetTotal(grossTotal.add(totalTax).add(otherCharges));

        // Updates balance based on amount received if provided by logic or UI
        // Here we just persist what is given or simple calc
        if (invoice.getAmountReceived() != null) {
            invoice.setBalanceDue(invoice.getNetTotal().subtract(invoice.getAmountReceived()));
        } else {
            invoice.setBalanceDue(invoice.getNetTotal());
        }
    }

    private SalesInvoiceResponse mapEntityToResponse(SalesInvoice invoice) {
        SalesInvoiceResponse response = new SalesInvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceLedger(invoice.getInvoiceLedger());
        response.setInvoiceDate(invoice.getInvoiceDate());

        if (invoice.getCustomer() != null) {
            response.setCustomerId(invoice.getCustomer().getId());
            response.setCustomerName(invoice.getCustomer().getCompanyName());
        }

        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setReference(invoice.getReference());
        response.setOrderNumber(invoice.getOrderNumber());
        response.setDueDate(invoice.getDueDate());
        response.setDateOfSupply(invoice.getDateOfSupply());

        if (invoice.getSalesperson() != null) {
            response.setSalespersonId(invoice.getSalesperson().getId());
            response.setSalespersonName(
                    invoice.getSalesperson().getFirstName() + " " + invoice.getSalesperson().getLastName());
        }

        response.setEnableGrossNetWeight(invoice.getEnableGrossNetWeight());
        response.setDelayReason(invoice.getDelayReason());
        response.setAmountReceived(invoice.getAmountReceived());
        response.setBalanceDue(invoice.getBalanceDue());

        response.setSubTotal(invoice.getSubTotal());
        response.setTotalDiscount(invoice.getTotalDiscount());
        response.setGrossTotal(invoice.getGrossTotal());
        response.setTotalTax(invoice.getTotalTax());
        response.setOtherCharges(invoice.getOtherCharges());
        response.setNetTotal(invoice.getNetTotal());

        response.setAttachments(invoice.getAttachments());
        response.setTermsAndConditions(invoice.getTermsAndConditions());
        response.setNotes(invoice.getNotes());
        response.setTemplate(invoice.getTemplate());
        response.setEmailTo(invoice.getEmailTo());
        response.setStatus(invoice.getStatus());

        response.setCreatedBy(invoice.getCreatedBy());
        response.setUpdatedBy(invoice.getUpdatedBy());

        if (invoice.getItems() != null) {
            response.setItems(invoice.getItems().stream().map(item -> {
                SalesInvoiceItemResponse itemResponse = new SalesInvoiceItemResponse();
                itemResponse.setId(item.getId());
                if (item.getCrmProduct() != null) {
                    itemResponse.setCrmProductId(item.getCrmProduct().getId());
                }
                itemResponse.setItemCode(item.getItemCode());
                itemResponse.setItemName(item.getItemName());
                itemResponse.setDescription(item.getDescription());
                itemResponse.setPackingType(item.getPackingType());

                itemResponse.setQuantityGross(item.getQuantityGross());
                itemResponse.setQuantityNet(item.getQuantityNet());
                itemResponse.setSendQuantity(item.getSendQuantity());
                itemResponse.setInvoiceQuantity(item.getInvoiceQuantity());

                itemResponse.setRate(item.getRate());
                itemResponse.setAmount(item.getAmount());
                itemResponse.setTaxValue(item.getTaxValue());
                itemResponse.setTaxExempt(item.isTaxExempt());
                itemResponse.setTaxPercentage(item.getTaxPercentage());

                return itemResponse;
            }).collect(Collectors.toList()));
        }

        return response;
    }
}
