package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesInvoiceLineRequest;
import com.example.multi_tanent.sales.dto.SalesInvoiceLineResponse;
import com.example.multi_tanent.sales.dto.SalesInvoiceRequest;
import com.example.multi_tanent.sales.dto.SalesInvoiceResponse;
import com.example.multi_tanent.sales.entity.*;
import com.example.multi_tanent.sales.repository.*;
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
public class SalesInvoiceService {

    private final SalesInvoiceRepository invoiceRepo;
    private final SalesOrderRepository orderRepo;
    private final SaleCustomerRepository customerRepo;
    private final SaleProductRepository productRepo;

    public SalesInvoiceResponse create(SalesInvoiceRequest req) {
        if (invoiceRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Invoice with number '" + req.getNumber() + "' already exists.");
        }

        SalesInvoice invoice = new SalesInvoice();
        applyRequestToEntity(req, invoice);
        recalculateTotals(invoice);

        return toResponse(invoiceRepo.save(invoice));
    }

    public SalesInvoiceResponse update(Long id, SalesInvoiceRequest req) {
        SalesInvoice invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));

        if (!invoice.getNumber().equals(req.getNumber()) && invoiceRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Invoice with number '" + req.getNumber() + "' already exists.");
        }

        applyRequestToEntity(req, invoice);
        recalculateTotals(invoice);
        return toResponse(invoiceRepo.save(invoice));
    }

    @Transactional(readOnly = true)
    public SalesInvoiceResponse getById(Long id) {
        return invoiceRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalesInvoiceResponse> getAll() {
        return invoiceRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalesInvoice invoice = invoiceRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + id));
        invoiceRepo.delete(invoice);
    }

    public void recalculateInvoiceTotals(Long invoiceId) {
        SalesInvoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceId));
        recalculateTotals(invoice);
        invoiceRepo.save(invoice);
    }

    private void applyRequestToEntity(SalesInvoiceRequest req, SalesInvoice entity) {
        entity.setNumber(req.getNumber());
        entity.setDate(req.getDate());
        entity.setDueDate(req.getDueDate());
        entity.setCurrency(req.getCurrency());
        entity.setStatus(req.getStatus());
        entity.setType(req.getType());
        entity.setNotes(req.getNotes());

        if (req.getCustomerId() != null) {
            SaleCustomer customer = customerRepo.findById(req.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.getCustomerId()));
            entity.setCustomer(customer);
        }

        if (req.getSourceSalesOrderId() != null) {
            SalesOrder order = orderRepo.findById(req.getSourceSalesOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Source Sales Order not found: " + req.getSourceSalesOrderId()));
            entity.setSourceSalesOrder(order);
        }

        entity.getLines().clear();
        if (req.getLines() != null) {
            for (SalesInvoiceLineRequest lineReq : req.getLines()) {
                SalesInvoiceLine line = new SalesInvoiceLine();
                line.setInvoice(entity);

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
                BigDecimal lineTotal = (line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO)
                        .multiply(line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
                        .subtract(line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO);
                line.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
                entity.getLines().add(line);
            }
        }
    }

    private void recalculateTotals(SalesInvoice entity) {
        BigDecimal subtotal = entity.getLines().stream().map(SalesInvoiceLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = entity.getLines().stream()
                .filter(l -> l.getTaxRate() != null && l.getTaxRate().compareTo(BigDecimal.ZERO) > 0)
                .map(l -> l.getLineTotal().multiply(l.getTaxRate().divide(new BigDecimal("100"))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        entity.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        entity.setTaxTotal(taxTotal.setScale(2, RoundingMode.HALF_UP));
        entity.setGrandTotal(subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private SalesInvoiceResponse toResponse(SalesInvoice e) {
        List<SalesInvoiceLineResponse> lineResponses = e.getLines().stream().map(this::toLineResponse).collect(Collectors.toList());
        return SalesInvoiceResponse.builder().id(e.getId()).number(e.getNumber()).date(e.getDate()).dueDate(e.getDueDate()).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).currency(e.getCurrency()).status(e.getStatus()).type(e.getType()).subtotal(e.getSubtotal()).taxTotal(e.getTaxTotal()).grandTotal(e.getGrandTotal()).notes(e.getNotes()).sourceSalesOrderId(e.getSourceSalesOrder() != null ? e.getSourceSalesOrder().getId() : null).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion()).lines(lineResponses).build();
    }

    private SalesInvoiceLineResponse toLineResponse(SalesInvoiceLine l) {
        return SalesInvoiceLineResponse.builder().id(l.getId()).productId(l.getProduct() != null ? l.getProduct().getId() : null).productName(l.getProduct() != null ? l.getProduct().getName() : null).description(l.getDescription()).quantity(l.getQuantity()).unitPrice(l.getUnitPrice()).discount(l.getDiscount()).taxRate(l.getTaxRate()).lineTotal(l.getLineTotal()).build();
    }
}