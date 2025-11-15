package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesQuotationLineRequest;
import com.example.multi_tanent.sales.dto.SalesQuotationLineResponse;
import com.example.multi_tanent.sales.dto.SalesQuotationRequest;
import com.example.multi_tanent.sales.dto.SalesQuotationResponse;
import com.example.multi_tanent.sales.entity.SaleCustomer;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.entity.SalesQuotation;
import com.example.multi_tanent.sales.entity.SalesQuotationLine;
import com.example.multi_tanent.sales.repository.SaleCustomerRepository;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.sales.repository.SalesQuotationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesQuotationService {

    private final SalesQuotationRepository quotationRepo;
    private final SaleCustomerRepository customerRepo;
    private final SaleProductRepository productRepo;

    public SalesQuotationResponse create(SalesQuotationRequest req) {
        if (quotationRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Quotation with number '" + req.getNumber() + "' already exists.");
        }

        SalesQuotation quotation = new SalesQuotation();
        applyRequestToEntity(req, quotation);
        recalculateTotals(quotation);

        return toResponse(quotationRepo.save(quotation));
    }

    public SalesQuotationResponse update(Long id, SalesQuotationRequest req) {
        SalesQuotation quotation = quotationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id));

        if (!quotation.getNumber().equals(req.getNumber()) && quotationRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Quotation with number '" + req.getNumber() + "' already exists.");
        }

        applyRequestToEntity(req, quotation);
        recalculateTotals(quotation);
        return toResponse(quotationRepo.save(quotation));
    }

    @Transactional(readOnly = true)
    public SalesQuotationResponse getById(Long id) {
        return quotationRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalesQuotationResponse> getAll() {
        return quotationRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalesQuotation quotation = quotationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + id));
        quotationRepo.delete(quotation);
    }

    public void recalculateQuotationTotals(Long quotationId) {
        SalesQuotation quotation = quotationRepo.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + quotationId));
        recalculateTotals(quotation);
        quotationRepo.save(quotation);
    }

    private void applyRequestToEntity(SalesQuotationRequest req, SalesQuotation entity) {
        entity.setNumber(req.getNumber());
        entity.setDate(req.getDate());
        entity.setExpiryDate(req.getExpiryDate());
        entity.setCurrency(req.getCurrency());
        entity.setStatus(req.getStatus());
        entity.setNotes(req.getNotes());

        if (req.getCustomerId() != null) {
            // The SaleCustomerRepository seems to be for 'SaleContact', which is likely a typo.
            // I'll assume it should be SaleCustomer and that it has a findByIdAndTenantId method.
            SaleCustomer customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.getCustomerId()));
            entity.setCustomer(customer);
        } else {
            entity.setCustomer(null);
        }

        entity.getItems().clear();
        if (req.getItems() != null) {
            for (SalesQuotationLineRequest lineReq : req.getItems()) {
                SalesQuotationLine line = new SalesQuotationLine();
                line.setQuotation(entity);

                if (lineReq.getProductId() != null) {
                    SaleProduct product = productRepo.findById(lineReq.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + lineReq.getProductId()));
                    line.setProduct(product);
                }
                line.setDescription(lineReq.getDescription());
                line.setQuantity(lineReq.getQuantity() != null ? lineReq.getQuantity() : BigDecimal.ZERO);
                line.setUnitPrice(lineReq.getUnitPrice() != null ? lineReq.getUnitPrice() : BigDecimal.ZERO);
                line.setDiscount(lineReq.getDiscount() != null ? lineReq.getDiscount() : BigDecimal.ZERO);
                line.setTaxRate(lineReq.getTaxRate());
                BigDecimal lineTotal = line.getUnitPrice().multiply(line.getQuantity()).subtract(line.getDiscount());
                line.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
                entity.getItems().add(line);
            }
        }
    }

    private void recalculateTotals(SalesQuotation entity) {
        BigDecimal subtotal = entity.getItems().stream().map(SalesQuotationLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = entity.getItems().stream()
                .filter(l -> l.getTaxRate() != null && l.getTaxRate().compareTo(BigDecimal.ZERO) > 0)
                .map(l -> l.getLineTotal().multiply(l.getTaxRate().divide(new BigDecimal("100"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        entity.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        entity.setTaxTotal(taxTotal.setScale(2, RoundingMode.HALF_UP));
        entity.setGrandTotal(subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private SalesQuotationResponse toResponse(SalesQuotation e) {
        List<SalesQuotationLineResponse> lineResponses = e.getItems().stream().map(this::toLineResponse).collect(Collectors.toList());
        return SalesQuotationResponse.builder().id(e.getId()).number(e.getNumber()).date(e.getDate()).expiryDate(e.getExpiryDate()).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).currency(e.getCurrency()).status(e.getStatus()).subtotal(e.getSubtotal()).taxTotal(e.getTaxTotal()).grandTotal(e.getGrandTotal()).notes(e.getNotes()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion()).items(lineResponses).build();
    }

    private SalesQuotationLineResponse toLineResponse(SalesQuotationLine l) {
        return SalesQuotationLineResponse.builder().id(l.getId()).productId(l.getProduct() != null ? l.getProduct().getId() : null).productName(l.getProduct() != null ? l.getProduct().getName() : null).description(l.getDescription()).quantity(l.getQuantity()).unitPrice(l.getUnitPrice()).discount(l.getDiscount()).taxRate(l.getTaxRate()).lineTotal(l.getLineTotal()).build();
    }
}