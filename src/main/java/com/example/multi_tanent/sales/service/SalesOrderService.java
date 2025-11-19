package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.production.repository.ProTaxRepository;
import com.example.multi_tanent.production.repository.ProUnitRepository;
import com.example.multi_tanent.sales.dto.*;
import com.example.multi_tanent.sales.entity.*;
import com.example.multi_tanent.sales.enums.DocumentStatus;
import com.example.multi_tanent.sales.enums.DocumentType;
import com.example.multi_tanent.sales.repository.*;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesOrderService {

    private final SalesOrderRepository orderRepo;
    private final SalesQuotationRepository quotationRepo;
    private final SaleCustomerRepository customerRepo;
    private final SaleProductRepository productRepo;
    private final ProUnitRepository unitRepo;
    private final ProTaxRepository taxRepo;
    private final SalesTermAndConditionRepository termAndConditionRepo;
    private final SalesAttachmentRepository attachmentRepo;
    private final TenantRepository tenantRepo;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepo.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }

    public SalesOrderResponse create(SalesOrderRequest req) {
        if (orderRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Sales Order with number '" + req.getNumber() + "' already exists.");
        }

        SalesOrder order = new SalesOrder();
        applyRequestToEntity(req, order);
        recalculateTotals(order);

        return toResponse(orderRepo.save(order));
    }

    @Transactional
    public SalesOrderResponse createFromQuotation(Long quotationId) {
        // 1. Find the source quotation
        SalesQuotation quotation = quotationRepo.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + quotationId));

        // 2. Create a new SalesOrder
        SalesOrder order = new SalesOrder();
        order.setNumber("SO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()); // Generate a temporary unique number
        order.setDate(java.time.LocalDate.now());
        order.setStatus(DocumentStatus.DRAFT);

        // 3. Copy header details
        order.setCustomer(quotation.getCustomer());
        order.setCurrency(quotation.getCurrency());
        order.setNotes("Created from Quotation: " + quotation.getNumber());
        order.setTermAndCondition(quotation.getTermAndCondition());
        order.setSourceQuotation(quotation);

        // 4. Copy line items
        for (SalesQuotationLine quoteLine : quotation.getItems()) {
            SalesOrderLine orderLine = new SalesOrderLine();
            orderLine.setSalesOrder(order);
            orderLine.setProduct(quoteLine.getProduct());
            orderLine.setDescription(quoteLine.getDescription());
            orderLine.setQuantity(quoteLine.getQuantity());
            orderLine.setUnitPrice(quoteLine.getUnitPrice());
            orderLine.setDiscount(quoteLine.getDiscount());
            orderLine.setUnit(quoteLine.getUnit());
            orderLine.setTax(quoteLine.getTax());
            orderLine.setLineTotal(quoteLine.getLineTotal());
            order.getLines().add(orderLine);
        }

        recalculateTotals(order);
        return toResponse(orderRepo.save(order));
    }

    public SalesOrderResponse update(Long id, SalesOrderRequest req) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + id));

        if (!order.getNumber().equals(req.getNumber()) && orderRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Sales Order with number '" + req.getNumber() + "' already exists.");
        }

        applyRequestToEntity(req, order);
        recalculateTotals(order);
        return toResponse(orderRepo.save(order));
    }

    @Transactional
    public void markAsInvoiced(Long orderId) {
        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + orderId));
        order.setStatus(DocumentStatus.INVOICED);
        orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse getById(Long id) {
        return orderRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalesOrderResponse> getAll() {
        return orderRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalesOrder order = orderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + id));
        orderRepo.delete(order);
    }

    public void recalculateOrderTotals(Long orderId) {
        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + orderId));
        recalculateTotals(order);
        orderRepo.save(order);
    }

    private void applyRequestToEntity(SalesOrderRequest req, SalesOrder entity) {
        entity.setNumber(req.getNumber());
        entity.setDate(req.getDate());
        entity.setCurrency(req.getCurrency());
        entity.setStatus(req.getStatus());
        entity.setNotes(req.getNotes());

        if (req.getCustomerId() != null) {
            SaleCustomer customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.getCustomerId()));
            entity.setCustomer(customer);
        }

        if (req.getSourceQuotationId() != null) {
            SalesQuotation quotation = quotationRepo.findById(req.getSourceQuotationId())
                    .orElseThrow(() -> new EntityNotFoundException("Source Quotation not found: " + req.getSourceQuotationId()));
            entity.setSourceQuotation(quotation);
        }

        if (req.getTermAndConditionId() != null) {
            Long tenantId = getCurrentTenant().getId();
            entity.setTermAndCondition(termAndConditionRepo.findByTenantIdAndId(tenantId, req.getTermAndConditionId()).orElse(null));
        } else {
            entity.setTermAndCondition(null);
        }

        entity.getLines().clear();
        if (req.getLines() != null) {
            for (SalesOrderLineRequest lineReq : req.getLines()) {
                SalesOrderLine line = new SalesOrderLine();
                line.setSalesOrder(entity);

                if (lineReq.getProductId() != null) {
                    SaleProduct product = productRepo.findById(lineReq.getProductId())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + lineReq.getProductId()));
                    line.setProduct(product);
                }
                line.setDescription(lineReq.getDescription());

                if (lineReq.getUnitId() != null) {
                    line.setUnit(unitRepo.findById(lineReq.getUnitId())
                            .orElseThrow(() -> new EntityNotFoundException("Unit not found: " + lineReq.getUnitId())));
                }

                if (lineReq.getTaxId() != null) {
                    line.setTax(taxRepo.findById(lineReq.getTaxId())
                            .orElseThrow(() -> new EntityNotFoundException("Tax not found: " + lineReq.getTaxId())));
                }

                line.setQuantity(lineReq.getQuantity() != null ? lineReq.getQuantity() : BigDecimal.ZERO);
                line.setUnitPrice(lineReq.getUnitPrice() != null ? lineReq.getUnitPrice() : BigDecimal.ZERO);
                line.setDiscount(lineReq.getDiscount() != null ? lineReq.getDiscount() : BigDecimal.ZERO);

                BigDecimal lineTotal = (line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO)
                        .multiply(line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
                        .subtract(line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO);
                line.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
                entity.getLines().add(line);
            }
        }
    }

    private void recalculateTotals(SalesOrder entity) {
        BigDecimal subtotal = entity.getLines().stream().map(SalesOrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = entity.getLines().stream()
                .filter(l -> l.getTax() != null && l.getTax().getRate() != null && l.getTax().getRate().compareTo(BigDecimal.ZERO) > 0)
                .map(l -> l.getLineTotal().multiply(l.getTax().getRate().divide(new BigDecimal("100"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        entity.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        entity.setTaxTotal(taxTotal.setScale(2, RoundingMode.HALF_UP));
        entity.setGrandTotal(subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private SalesOrderResponse toResponse(SalesOrder e) {
        List<SalesOrderLineResponse> lineResponses = e.getLines().stream().map(this::toLineResponse).collect(Collectors.toList());
        List<SalesAttachmentResponse> attachmentResponses = attachmentRepo.findByDocTypeAndDocId(DocumentType.ORDER, e.getId())
                .stream().map(SalesAttachmentResponse::fromEntity).collect(Collectors.toList());

        return SalesOrderResponse.builder()
                .id(e.getId()).number(e.getNumber()).date(e.getDate())
                .customerId(e.getCustomer() != null ? e.getCustomer().getId() : null)
                .customerName(e.getCustomer() != null ? e.getCustomer().getName() : null)
                .currency(e.getCurrency()).status(e.getStatus()).subtotal(e.getSubtotal())
                .taxTotal(e.getTaxTotal()).grandTotal(e.getGrandTotal()).notes(e.getNotes())
                .sourceQuotationId(e.getSourceQuotation() != null ? e.getSourceQuotation().getId() : null)
                .termAndCondition(SalesTermAndConditionResponse.fromEntity(e.getTermAndCondition()))
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion())
                .lines(lineResponses)
                .attachments(attachmentResponses)
                .build();
    }

    private SalesOrderLineResponse toLineResponse(SalesOrderLine l) {
        return SalesOrderLineResponse.builder()
                .id(l.getId()).productId(l.getProduct() != null ? l.getProduct().getId() : null)
                .productName(l.getProduct() != null ? l.getProduct().getName() : null)
                .description(l.getDescription()).quantity(l.getQuantity()).unitPrice(l.getUnitPrice())
                .unitId(l.getUnit() != null ? l.getUnit().getId() : null)
                .unitName(l.getUnit() != null ? l.getUnit().getName() : null)
                .discount(l.getDiscount())
                .taxId(l.getTax() != null ? l.getTax().getId() : null)
                .taxCode(l.getTax() != null ? l.getTax().getCode() : null)
                .taxRate(l.getTax() != null ? l.getTax().getRate() : null)
                .lineTotal(l.getLineTotal()).build();
    }
}
