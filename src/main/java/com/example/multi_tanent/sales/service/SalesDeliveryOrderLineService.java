package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineRequest;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineResponse;
import com.example.multi_tanent.sales.entity.SaleProduct;
import com.example.multi_tanent.sales.entity.SalesDeliveryOrder;
import com.example.multi_tanent.sales.entity.SalesDeliveryOrderLine;
import com.example.multi_tanent.sales.entity.SalesOrderLine;
import com.example.multi_tanent.sales.repository.SaleProductRepository;
import com.example.multi_tanent.sales.repository.SalesDeliveryOrderLineRepository;
import com.example.multi_tanent.sales.repository.SalesDeliveryOrderRepository;
import com.example.multi_tanent.sales.repository.SalesOrderLineRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesDeliveryOrderLineService {

    private final SalesDeliveryOrderRepository doRepo;
    private final SalesDeliveryOrderLineRepository doLineRepo;
    private final SaleProductRepository productRepo;
    private final SalesOrderLineRepository orderLineRepo;

    public SalesDeliveryOrderLineResponse addLine(Long deliveryOrderId, SalesDeliveryOrderLineRequest req) {
        SalesDeliveryOrder deliveryOrder = doRepo.findById(deliveryOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order not found: " + deliveryOrderId));

        SalesDeliveryOrderLine line = new SalesDeliveryOrderLine();
        line.setDeliveryOrder(deliveryOrder);
        applyRequestToEntity(req, line);

        return toResponse(doLineRepo.save(line));
    }

    @Transactional(readOnly = true)
    public List<SalesDeliveryOrderLineResponse> getAllLinesForDeliveryOrder(Long deliveryOrderId) {
        SalesDeliveryOrder deliveryOrder = doRepo.findById(deliveryOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order not found: " + deliveryOrderId));
        return deliveryOrder.getLines().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public void deleteLine(Long lineId) {
        SalesDeliveryOrderLine line = doLineRepo.findById(lineId)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order Line not found: " + lineId));
        doLineRepo.delete(line);
    }

    private void applyRequestToEntity(SalesDeliveryOrderLineRequest req, SalesDeliveryOrderLine entity) {
        if (req.getProductId() != null) {
            SaleProduct product = productRepo.findById(req.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.getProductId()));
            entity.setProduct(product);
        }
        if (req.getSourceOrderLineId() != null) {
            SalesOrderLine soLine = orderLineRepo.findById(req.getSourceOrderLineId())
                    .orElseThrow(() -> new EntityNotFoundException("Source Order Line not found: " + req.getSourceOrderLineId()));
            entity.setSourceOrderLine(soLine);
        }
        entity.setDescription(req.getDescription());
        entity.setQuantity(req.getQuantity() != null ? req.getQuantity() : BigDecimal.ZERO);
    }

    private SalesDeliveryOrderLineResponse toResponse(SalesDeliveryOrderLine l) {
        return SalesDeliveryOrderLineResponse.builder().id(l.getId()).productId(l.getProduct() != null ? l.getProduct().getId() : null).productName(l.getProduct() != null ? l.getProduct().getName() : null).description(l.getDescription()).quantity(l.getQuantity()).sourceOrderLineId(l.getSourceOrderLine() != null ? l.getSourceOrderLine().getId() : null).build();
    }
}
