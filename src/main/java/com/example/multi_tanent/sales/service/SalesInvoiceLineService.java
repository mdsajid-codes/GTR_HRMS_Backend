package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.production.repository.ProTaxRepository;
import com.example.multi_tanent.production.repository.ProUnitRepository;
import com.example.multi_tanent.sales.dto.SalesInvoiceLineRequest;
import com.example.multi_tanent.sales.dto.SalesInvoiceLineResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.entity.SalesInvoice;
import com.example.multi_tanent.sales.entity.SalesInvoiceLine;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.sales.repository.SalesInvoiceLineRepository;
import com.example.multi_tanent.sales.repository.SalesInvoiceRepository;
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
public class SalesInvoiceLineService {

    private final SalesInvoiceRepository invoiceRepo;
    private final SalesInvoiceLineRepository lineRepo;
    private final SaleProductRepository productRepo;
    private final ProUnitRepository unitRepo;
    private final ProTaxRepository taxRepo;
    private final SalesInvoiceService invoiceService; // To recalculate totals

    public SalesInvoiceLineResponse addLine(Long invoiceId, SalesInvoiceLineRequest req) {
        SalesInvoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceId));

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setInvoice(invoice);
        applyRequestToEntity(req, line);

        SalesInvoiceLine savedLine = lineRepo.save(line);
        invoiceService.recalculateInvoiceTotals(invoiceId);
        return toResponse(savedLine);
    }

    public SalesInvoiceLineResponse updateLine(Long lineId, SalesInvoiceLineRequest req) {
        SalesInvoiceLine line = lineRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice Line not found: " + lineId));

        applyRequestToEntity(req, line);
        SalesInvoiceLine savedLine = lineRepo.save(line);
        invoiceService.recalculateInvoiceTotals(line.getInvoice().getId());
        return toResponse(savedLine);
    }

    @Transactional(readOnly = true)
    public List<SalesInvoiceLineResponse> getAllLinesForInvoice(Long invoiceId) {
        SalesInvoice invoice = invoiceRepo.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found: " + invoiceId));
        return invoice.getLines().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void deleteLine(Long lineId) {
        SalesInvoiceLine line = lineRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Invoice Line not found: " + lineId));

        Long invoiceId = line.getInvoice().getId();
        lineRepo.delete(line);
        invoiceService.recalculateInvoiceTotals(invoiceId);
    }

    private void applyRequestToEntity(SalesInvoiceLineRequest req, SalesInvoiceLine line) {
        if (req.getProductId() != null) {
            SaleProduct product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.getProductId()));
            line.setProduct(product);
        }

        line.setDescription(req.getDescription());

        if (req.getUnitId() != null) {
            line.setUnit(unitRepo.findById(req.getUnitId())
                    .orElseThrow(() -> new EntityNotFoundException("Unit not found: " + req.getUnitId())));
        } else {
            line.setUnit(null);
        }

        if (req.getTaxId() != null) {
            line.setTax(taxRepo.findById(req.getTaxId())
                    .orElseThrow(() -> new EntityNotFoundException("Tax not found: " + req.getTaxId())));
        } else {
            line.setTax(null);
        }

        line.setQuantity(req.getQuantity() != null ? req.getQuantity() : BigDecimal.ZERO);
        line.setUnitPrice(req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO);
        line.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO);

        BigDecimal lineTotal = (line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO)
                .multiply(line.getQuantity() != null ? line.getQuantity() : BigDecimal.ZERO)
                .subtract(line.getDiscount() != null ? line.getDiscount() : BigDecimal.ZERO);
        line.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
    }

    private SalesInvoiceLineResponse toResponse(SalesInvoiceLine l) {
        return SalesInvoiceLineResponse.builder()
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
