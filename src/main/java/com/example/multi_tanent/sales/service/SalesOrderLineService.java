package com.example.multi_tanent.sales.service;



import com.example.multi_tanent.production.repository.ProTaxRepository;
import com.example.multi_tanent.production.repository.ProUnitRepository;
import com.example.multi_tanent.sales.dto.SalesOrderLineRequest;
import com.example.multi_tanent.sales.dto.SalesOrderLineResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.entity.SalesOrder;
import com.example.multi_tanent.sales.entity.SalesOrderLine;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.sales.repository.SalesOrderLineRepository;
import com.example.multi_tanent.sales.repository.SalesOrderRepository;
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
public class SalesOrderLineService {

    private final SalesOrderRepository orderRepo;
    private final SalesOrderLineRepository lineRepo;
    private final SaleProductRepository productRepo;
    private final ProUnitRepository unitRepo;
    private final ProTaxRepository taxRepo;
    private final SalesOrderService orderService; // To recalculate totals

    public SalesOrderLineResponse addLine(Long orderId, SalesOrderLineRequest req) {
        SalesOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order not found: " + orderId));

        SalesOrderLine line = new SalesOrderLine();
        line.setSalesOrder(order);
        applyRequestToEntity(req, line);

        SalesOrderLine savedLine = lineRepo.save(line);
        orderService.recalculateOrderTotals(orderId);
        return toResponse(savedLine);
    }

    public SalesOrderLineResponse updateLine(Long lineId, SalesOrderLineRequest req) {
        SalesOrderLine line = lineRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order Line not found: " + lineId));

        applyRequestToEntity(req, line);
        SalesOrderLine savedLine = lineRepo.save(line);
        orderService.recalculateOrderTotals(line.getSalesOrder().getId());
        return toResponse(savedLine);
    }

    @Transactional(readOnly = true)
    public List<SalesOrderLineResponse> getAllLinesForOrder(Long orderId) {
        if (!orderRepo.existsById(orderId)) {
            throw new EntityNotFoundException("Sales Order not found: " + orderId);
        }
        return orderRepo.findById(orderId).get().getLines()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteLine(Long lineId) {
        SalesOrderLine line = lineRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Sales Order Line not found: " + lineId));

        Long orderId = line.getSalesOrder().getId();
        lineRepo.delete(line);
        orderService.recalculateOrderTotals(orderId);
   }

    private void applyRequestToEntity(SalesOrderLineRequest req, SalesOrderLine line) {
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

    private SalesOrderLineResponse toResponse(SalesOrderLine l) {
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
