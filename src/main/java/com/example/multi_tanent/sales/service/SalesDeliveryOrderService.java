package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineRequest;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderLineResponse;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderRequest;
import com.example.multi_tanent.sales.dto.SalesDeliveryOrderResponse;
import com.example.multi_tanent.sales.entity.*;
import com.example.multi_tanent.sales.repository.*;
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
public class SalesDeliveryOrderService {

    private final SalesDeliveryOrderRepository doRepo;
    private final SalesOrderRepository orderRepo;
    private final SalesOrderLineRepository orderLineRepo;
    private final SaleCustomerRepository customerRepo;
    private final SaleProductRepository productRepo;

    public SalesDeliveryOrderResponse create(SalesDeliveryOrderRequest req) {
        if (doRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Delivery Order with number '" + req.getNumber() + "' already exists.");
        }

        SalesDeliveryOrder deliveryOrder = new SalesDeliveryOrder();
        applyRequestToEntity(req, deliveryOrder);

        return toResponse(doRepo.save(deliveryOrder));
    }

    public SalesDeliveryOrderResponse update(Long id, SalesDeliveryOrderRequest req) {
        SalesDeliveryOrder deliveryOrder = doRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order not found: " + id));

        if (!deliveryOrder.getNumber().equals(req.getNumber()) && doRepo.existsByNumber(req.getNumber())) {
            throw new IllegalArgumentException("Delivery Order with number '" + req.getNumber() + "' already exists.");
        }

        applyRequestToEntity(req, deliveryOrder);
        return toResponse(doRepo.save(deliveryOrder));
    }

    @Transactional(readOnly = true)
    public SalesDeliveryOrderResponse getById(Long id) {
        return doRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SalesDeliveryOrderResponse> getAll() {
        return doRepo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        SalesDeliveryOrder deliveryOrder = doRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Delivery Order not found: " + id));
        doRepo.delete(deliveryOrder);
    }

    private void applyRequestToEntity(SalesDeliveryOrderRequest req, SalesDeliveryOrder entity) {
        entity.setNumber(req.getNumber());
        entity.setDate(req.getDate());
        entity.setStatus(req.getStatus());
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
            for (SalesDeliveryOrderLineRequest lineReq : req.getLines()) {
                SalesDeliveryOrderLine line = new SalesDeliveryOrderLine();
                line.setDeliveryOrder(entity);
                // Mapping for line items is simpler as they don't affect totals on the DO
                line.setQuantity(lineReq.getQuantity() != null ? lineReq.getQuantity() : BigDecimal.ZERO);
                entity.getLines().add(line);
            }
        }
    }

    private SalesDeliveryOrderResponse toResponse(SalesDeliveryOrder e) {
        List<SalesDeliveryOrderLineResponse> lineResponses = e.getLines().stream().map(this::toLineResponse).collect(Collectors.toList());
        return SalesDeliveryOrderResponse.builder().id(e.getId()).number(e.getNumber()).date(e.getDate()).customerId(e.getCustomer() != null ? e.getCustomer().getId() : null).customerName(e.getCustomer() != null ? e.getCustomer().getName() : null).status(e.getStatus()).notes(e.getNotes()).sourceSalesOrderId(e.getSourceSalesOrder() != null ? e.getSourceSalesOrder().getId() : null).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).version(e.getVersion()).lines(lineResponses).build();
    }

    private SalesDeliveryOrderLineResponse toLineResponse(SalesDeliveryOrderLine l) {
        return SalesDeliveryOrderLineResponse.builder().id(l.getId()).productId(l.getProduct() != null ? l.getProduct().getId() : null).productName(l.getProduct() != null ? l.getProduct().getName() : null).description(l.getDescription()).quantity(l.getQuantity()).sourceOrderLineId(l.getSourceOrderLine() != null ? l.getSourceOrderLine().getId() : null).build();
    }
}
