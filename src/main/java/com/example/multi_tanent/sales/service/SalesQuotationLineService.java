package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesQuotationLineRequest;
import com.example.multi_tanent.sales.dto.SalesQuotationLineResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.entity.SalesQuotation;
import com.example.multi_tanent.sales.entity.SalesQuotationLine;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.sales.repository.SalesQuotationLineRepository;
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
public class SalesQuotationLineService {

    private final SalesQuotationRepository quotationRepo;
    private final SalesQuotationLineRepository itemRepo;
    private final SaleProductRepository productRepo;
    private final SalesQuotationService quotationService; // To recalculate totals

    public SalesQuotationLineResponse addLine(Long quotationId, SalesQuotationLineRequest req) {
        SalesQuotation quotation = quotationRepo.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + quotationId));

        SalesQuotationLine item = new SalesQuotationLine();
        item.setQuotation(quotation);
        applyRequestToEntity(req, item);

        SalesQuotationLine savedItem = itemRepo.save(item);
        quotationService.recalculateQuotationTotals(quotationId);
        return toResponse(savedItem);
    }

    public SalesQuotationLineResponse updateLine(Long lineId, SalesQuotationLineRequest req) {
        SalesQuotationLine item = itemRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation Item not found: " + lineId));

        applyRequestToEntity(req, item);
        SalesQuotationLine savedItem = itemRepo.save(item);
        quotationService.recalculateQuotationTotals(item.getQuotation().getId());
        return toResponse(savedItem);
    }

    @Transactional(readOnly = true)
    public SalesQuotationLineResponse getLineById(Long lineId) {
        return itemRepo.findById(lineId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Quotation Item not found: " + lineId));
    }

    @Transactional(readOnly = true)
    public List<SalesQuotationLineResponse> getAllLinesForQuotation(Long quotationId) {
        // Fetch the quotation first to ensure it exists for the current tenant.
        SalesQuotation quotation = quotationRepo.findById(quotationId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation not found: " + quotationId));
        return quotation.getItems()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteLine(Long lineId) {
        SalesQuotationLine item = itemRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Quotation Item not found: " + lineId));

        SalesQuotation quotation = item.getQuotation();
        itemRepo.delete(item);
        // The recalculation will be done in the same transaction,
        // so an explicit flush is not strictly necessary here.
        quotationService.recalculateQuotationTotals(quotation.getId());
    }

    private void applyRequestToEntity(SalesQuotationLineRequest req, SalesQuotationLine item) {
        if (req.getProductId() != null) {
            SaleProduct product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.getProductId()));
            item.setProduct(product);
        }
        item.setDescription(req.getDescription());
        item.setQuantity(req.getQuantity() != null ? req.getQuantity() : BigDecimal.ZERO);
        item.setUnitPrice(req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO);
        item.setDiscount(req.getDiscount() != null ? req.getDiscount() : BigDecimal.ZERO);
        item.setTaxRate(req.getTaxRate());
        // Line total calculation
        BigDecimal itemTotal = (item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
                .multiply(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO)
                .subtract(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
        item.setLineTotal(itemTotal.setScale(2, RoundingMode.HALF_UP));
    }

    private SalesQuotationLineResponse toResponse(SalesQuotationLine i) {
        return SalesQuotationLineResponse.builder().id(i.getId()).productId(i.getProduct() != null ? i.getProduct().getId() : null).productName(i.getProduct() != null ? i.getProduct().getName() : null).description(i.getDescription()).quantity(i.getQuantity()).unitPrice(i.getUnitPrice()).discount(i.getDiscount()).taxRate(i.getTaxRate()).lineTotal(i.getLineTotal()).build();
    }
}
