// package com.example.multi_tanent.purchases.service;

// import com.example.multi_tanent.config.TenantContext;
// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.entity.*;
// import com.example.multi_tanent.purchases.repository.*;
// import com.example.multi_tanent.spersusers.enitity.Tenant;
// import com.example.multi_tanent.spersusers.repository.TenantRepository;
// import com.example.multi_tanent.purchases.repository.PurPurchaseOrderRepository;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.*;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;
// import java.math.BigDecimal;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class PurGrnService {

//     private final PurGrnRepository grnRepo;
//     private final PurGrnItemRepository grnItemRepo;
//     private final PurPurchaseOrderRepository poRepo;
//     private final TenantRepository tenantRepo;

//     private Tenant currentTenant() {
//         String key = TenantContext.getTenantId();
//         return tenantRepo.findFirstByOrderByIdAsc()
//                 .orElseThrow(() -> new IllegalStateException("Tenant not resolved for key: " + key));
//     }

//     public PurGrnResponse create(PurGrnRequest req) {
//         Tenant t = currentTenant();
//         PurPurchaseOrder po = poRepo.findByIdAndTenantId(req.getPurchaseOrderId(), t.getId())
//                 .orElseThrow(
//                         () -> new EntityNotFoundException("Purchase order not found: " + req.getPurchaseOrderId()));

//         PurGrn grn = new PurGrn();
//         grn.setPurchaseOrder(po);
//         grn.setGrnNumber(req.getGrnNumber());
//         grn.setGrnDate(req.getGrnDate());
//         grn.setRemark(req.getRemark());
//         grn.setCreatedBy(req.getCreatedBy());
//         grn.setCreatedAt(LocalDateTime.now());

//         if (req.getItems() != null) {
//             for (PurGrnItemRequest ir : req.getItems()) {
//                 PurPurchaseOrderItem poItem = po.getItems().stream()
//                         .filter(i -> i.getId().equals(ir.getPurchaseOrderItemId()))
//                         .findFirst()
//                         .orElseThrow(
//                                 () -> new EntityNotFoundException("PO item not found: " + ir.getPurchaseOrderItemId()));

//                 PurGrnItem gi = new PurGrnItem();
//                 gi.setPurchaseOrderItem(poItem);
//                 gi.setReceivedQuantity(Optional.ofNullable(ir.getReceivedQuantity()).orElse(BigDecimal.ZERO));
//                 gi.setRate(ir.getRate());
//                 gi.setUnitId(ir.getUnitId());
//                 gi.setGrn(grn);
//                 grn.getItems().add(gi);

//                 // update poItem.receivedQuantity (add). Add field on PurPurchaseOrderItem if
//                 // not present.
//                 BigDecimal prevReceived = Optional.ofNullable(poItem.getReceivedQuantity()).orElse(BigDecimal.ZERO);
//                 poItem.setReceivedQuantity(prevReceived.add(gi.getReceivedQuantity()));
//             }
//         }

//         // Update PO status based on received quantities
//         boolean allReceived = po.getItems().stream()
//                 .allMatch(it -> {
//                     BigDecimal qty = Optional.ofNullable(it.getQuantity()).orElse(BigDecimal.ZERO);
//                     BigDecimal received = Optional.ofNullable(it.getReceivedQuantity()).orElse(BigDecimal.ZERO);
//                     return received.compareTo(qty) >= 0;
//                 });

//         if (allReceived)
//             po.setStatus("Received");
//         else
//             po.setStatus("Partially Received");

//         // save PO (cascade not necessary for GRN, but we'll save GRN explicitly)
//         PurGrn saved = grnRepo.save(grn);
//         // also persist PO changes
//         poRepo.save(po);

//         return toResponse(saved);
//     }

//     @Transactional(readOnly = true)
//     public PurGrnResponse getById(Long id) {
//         PurGrn g = grnRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("GRN not found: " + id));
//         return toResponse(g);
//     }

//     @Transactional(readOnly = true)
//     public List<PurGrnResponse> listByOrder(Long orderId) {
//         List<PurGrn> list = grnRepo.findByPurchaseOrder_Id(orderId);
//         return list.stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     @Transactional(readOnly = true)
//     public Page<PurGrnResponse> list(Pageable pageable) {
//         Tenant t = currentTenant();
//         return grnRepo.findByPurchaseOrder_Tenant_Id(t.getId(), pageable).map(this::toResponse);
//     }

//     /* ---------------- mapping ---------------- */
//     private PurGrnResponse toResponse(PurGrn g) {
//         PurGrnResponse.PurGrnResponseBuilder rb = PurGrnResponse.builder()
//                 .id(g.getId())
//                 .grnNumber(g.getGrnNumber())
//                 .grnDate(g.getGrnDate())
//                 .remark(g.getRemark())
//                 .createdBy(g.getCreatedBy())
//                 .createdAt(g.getCreatedAt());

//         if (g.getPurchaseOrder() != null) {
//             rb.purchaseOrderId(g.getPurchaseOrder().getId())
//                     .purchaseOrderNumber(g.getPurchaseOrder().getPoNumber());
//         }

//         List<PurGrnItemResponse> items = Optional.ofNullable(g.getItems()).orElse(Collections.emptyList())
//                 .stream()
//                 .map(it -> PurGrnItemResponse.builder()
//                         .id(it.getId())
//                         .purchaseOrderItemId(
//                                 it.getPurchaseOrderItem() != null ? it.getPurchaseOrderItem().getId() : null)
//                         .itemName(it.getPurchaseOrderItem() != null && it.getPurchaseOrderItem().getItem() != null
//                                 ? it.getPurchaseOrderItem().getItem().getName()
//                                 : null)
//                         .receivedQuantity(it.getReceivedQuantity())
//                         .rate(it.getRate())
//                         .unitId(it.getUnitId())
//                         .unitName(it.getPurchaseOrderItem() != null && it.getPurchaseOrderItem().getUnit() != null
//                                 ? it.getPurchaseOrderItem().getUnit().getName()
//                                 : null)
//                         .build())
//                 .collect(Collectors.toList());

//         rb.items(items);
//         return rb.build();
//     }
// }

// package com.example.multi_tanent.purchases.service;

// import com.example.multi_tanent.config.TenantContext;
// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.entity.*;
// import com.example.multi_tanent.purchases.repository.*;
// import com.example.multi_tanent.spersusers.enitity.Tenant;
// import com.example.multi_tanent.spersusers.repository.TenantRepository;
// import com.example.multi_tanent.spersusers.repository.PartyRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import jakarta.persistence.EntityNotFoundException;
// import java.time.LocalDateTime;
// import java.time.LocalDate;
// import java.math.BigDecimal;
// import java.util.List;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class PurGrnService {

//     private final PurGrnRepository grnRepo;
//     private final PurGrnItemRepository grnItemRepo;
//     private final PurPurchaseOrderRepository poRepo;
//     private final PurPurchaseOrderRepository purchaseOrderRepository; // alias to same repo used earlier
//     private final TenantRepository tenantRepo;

//     private Tenant currentTenant() {
//         String key = TenantContext.getTenantId();
//         return tenantRepo.findFirstByOrderByIdAsc()
//                 .orElseThrow(() -> new IllegalStateException("Tenant not resolved: " + key));
//     }

//     public PurGrnResponse create(PurGrnRequest req) {
//         Tenant t = currentTenant();
//         PurPurchaseOrder po = purchaseOrderRepository.findByIdAndTenantId(req.getPurchaseOrderId(), t.getId())
//                 .orElseThrow(
//                         () -> new EntityNotFoundException("Purchase order not found: " + req.getPurchaseOrderId()));

//         PurGrn grn = new PurGrn();
//         grn.setGrnNumber(req.getGrnNumber());
//         grn.setGrnDate(req.getGrnDate() != null ? req.getGrnDate() : LocalDate.now());
//         grn.setPurchaseOrder(po);
//         grn.setRemark(req.getRemark());
//         grn.setCreatedBy(req.getCreatedBy());
//         grn.setCreatedAt(LocalDateTime.now());

//         // add items
//         if (req.getItems() != null) {
//             for (PurGrnItemRequest ir : req.getItems()) {
//                 PurGrnItem gi = new PurGrnItem();
//                 gi.setPurchaseOrderItem(purchaseOrderRepository.getOne(ir.getPurchaseOrderItemId()).getItems().stream()
//                         .filter(i -> i.getId().equals(ir.getPurchaseOrderItemId()))
//                         .findFirst()
//                         .orElseThrow(() -> new EntityNotFoundException(
//                                 "PO Item not found: " + ir.getPurchaseOrderItemId()))); // note: better to use item repo
//                                                                                         // or query; simplified approach
//                                                                                         // below

//                 // safer fetch of PO item:
//                 PurPurchaseOrderItem poi = po.getItems().stream()
//                         .filter(i -> i.getId().equals(ir.getPurchaseOrderItemId()))
//                         .findFirst()
//                         .orElseThrow(
//                                 () -> new EntityNotFoundException("PO Item not found: " + ir.getPurchaseOrderItemId()));
//                 gi.setPurchaseOrderItem(poi);

//                 gi.setReceivedQuantity(ir.getReceivedQuantity() != null ? ir.getReceivedQuantity() : BigDecimal.ZERO);
//                 gi.setRate(ir.getRate());
//                 gi.setUnitId(ir.getUnitId());
//                 grn.addItem(gi);

//                 // update the PO item receivedQuantity -> add received
//                 BigDecimal prevReceived = OptionalOfBigDecimal(poi.getReceivedQuantity());
//                 poi.setReceivedQuantity(prevReceived.add(gi.getReceivedQuantity()));
//             }
//         }

//         PurGrn saved = grnRepo.save(grn);
//         // persist purchase order changes (po items updated via poRepo save)
//         poRepo.save(po);

//         return toResponse(saved);
//     }

//     private BigDecimal OptionalOfBigDecimal(BigDecimal b) {
//         return b == null ? BigDecimal.ZERO : b;
//     }

//     @Transactional(readOnly = true)
//     public List<PurGrnResponse> listByOrder(Long orderId) {
//         return grnRepo.findByPurchaseOrderId(orderId).stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     @Transactional(readOnly = true)
//     public PurGrnResponse getById(Long id) {
//         PurGrn g = grnRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("GRN not found: " + id));
//         return toResponse(g);
//     }

//     private PurGrnResponse toResponse(PurGrn g) {
//         return PurGrnResponse.builder()
//                 .id(g.getId())
//                 .grnNumber(g.getGrnNumber())
//                 .grnDate(g.getGrnDate())
//                 .purchaseOrderId(g.getPurchaseOrder() != null ? g.getPurchaseOrder().getId() : null)
//                 .remark(g.getRemark())
//                 .createdBy(g.getCreatedBy())
//                 .createdAt(g.getCreatedAt())
//                 .items(g.getItems().stream().map(i -> PurGrnItemResponse.builder()
//                         .id(i.getId())
//                         .purchaseOrderItemId(i.getPurchaseOrderItem() != null ? i.getPurchaseOrderItem().getId() : null)
//                         .itemName(i.getPurchaseOrderItem() != null && i.getPurchaseOrderItem().getItem() != null
//                                 ? i.getPurchaseOrderItem().getItem().getName()
//                                 : null)
//                         .receivedQuantity(i.getReceivedQuantity())
//                         .rate(i.getRate())
//                         .unitId(i.getUnitId())
//                         .build()).collect(Collectors.toList()))
//                 .build();
//     }
// }

// package com.example.multi_tanent.purchases.service;

// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.entity.*;
// import com.example.multi_tanent.purchases.repository.*;
// import com.example.multi_tanent.spersusers.repository.PartyRepository;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class PurGrnService {

//     private final PurGrnRepository grnRepo;
//     private final PurGrnItemRepository grnItemRepo;
//     private final PurPurchaseOrderRepository poRepo;
//     // other repos if needed

//     public PurGrnResponse create(PurGrnRequest req) {
//         if (req.getPurchaseOrderId() == null) {
//             throw new IllegalArgumentException("purchaseOrderId required");
//         }

//         PurPurchaseOrder po = poRepo.findById(req.getPurchaseOrderId())
//                 .orElseThrow(
//                         () -> new EntityNotFoundException("Purchase order not found: " + req.getPurchaseOrderId()));

//         PurGrn grn = PurGrn.builder()
//                 .grnNumber(req.getGrnNumber())
//                 .grnDate(req.getGrnDate())
//                 .purchaseOrder(po)
//                 .remark(req.getRemark())
//                 .createdBy(req.getCreatedBy())
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         // create items and update PO item receivedQuantity
//         if (req.getItems() != null) {
//             for (PurGrnItemRequest ir : req.getItems()) {
//                 // find the PO item
//                 PurPurchaseOrderItem poItem = po.getItems().stream()
//                         .filter(i -> Objects.equals(i.getId(), ir.getPurchaseOrderItemId()))
//                         .findFirst()
//                         .orElseThrow(
//                                 () -> new EntityNotFoundException("PO item not found: " + ir.getPurchaseOrderItemId()));

//                 // create GRN item
//                 PurGrnItem gi = PurGrnItem.builder()
//                         .purchaseOrderItem(poItem)
//                         .receivedQuantity(
//                                 Optional.ofNullable(ir.getReceivedQuantity()).orElse(java.math.BigDecimal.ZERO))
//                         .rate(ir.getRate())
//                         .unitId(ir.getUnitId())
//                         .build();

//                 grn.addItem(gi);

//                 // update the PO item received quantity
//                 java.math.BigDecimal prev = Optional.ofNullable(poItem.getReceivedQuantity())
//                         .orElse(java.math.BigDecimal.ZERO);
//                 java.math.BigDecimal add = Optional.ofNullable(ir.getReceivedQuantity())
//                         .orElse(java.math.BigDecimal.ZERO);
//                 poItem.setReceivedQuantity(prev.add(add));
//             }
//         }

//         // persist: because cascade=ALL on PurGrn.items, saving grn will cascade items
//         PurGrn saved = grnRepo.save(grn);

//         // also persist the updated PO (its items updated)
//         poRepo.save(po);

//         return toResponse(saved);
//     }

//     @Transactional(readOnly = true)
//     public List<PurGrnResponse> listByOrder(Long orderId) {
//         List<PurGrn> list = grnRepo.findByPurchaseOrderId(orderId);
//         return list.stream().map(this::toResponse).collect(Collectors.toList());
//     }

//     @Transactional(readOnly = true)
//     public PurGrnResponse getById(Long id) {
//         PurGrn g = grnRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("GRN not found: " + id));
//         return toResponse(g);
//     }

//     private PurGrnItemResponse itemToResponse(PurGrnItem it) {
//         return PurGrnItemResponse.builder()
//                 .id(it.getId())
//                 .purchaseOrderItemId(it.getPurchaseOrderItem() != null ? it.getPurchaseOrderItem().getId() : null)
//                 .itemName(it.getPurchaseOrderItem() != null && it.getPurchaseOrderItem().getItem() != null
//                         ? it.getPurchaseOrderItem().getItem().getName()
//                         : it.getPurchaseOrderItem() != null ? it.getPurchaseOrderItem().getDescription() : null)
//                 .receivedQuantity(it.getReceivedQuantity())
//                 .rate(it.getRate())
//                 .unitId(it.getUnitId())
//                 .build();
//     }

//     private PurGrnResponse toResponse(PurGrn g) {
//         return PurGrnResponse.builder()
//                 .id(g.getId())
//                 .grnNumber(g.getGrnNumber())
//                 .grnDate(g.getGrnDate())
//                 .purchaseOrderId(g.getPurchaseOrder() != null ? g.getPurchaseOrder().getId() : null)
//                 .remark(g.getRemark())
//                 .createdBy(g.getCreatedBy())
//                 .createdAt(g.getCreatedAt())
//                 .items(g.getItems().stream().map(this::itemToResponse).collect(Collectors.toList()))
//                 .build();
//     }
// }

// src/main/java/com/example/multi_tanent/purchases/service/PurGrnService.java
// package com.example.multi_tanent.purchases.service;

// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.entity.*;
// import com.example.multi_tanent.purchases.repository.*;
// import jakarta.persistence.EntityNotFoundException;
// import lombok.RequiredArgsConstructor;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class PurGrnService {

//         private final PurGrnRepository grnRepo;
//         private final PurGrnItemRepository grnItemRepo;
//         private final com.example.multi_tanent.purchases.repository.PurPurchaseOrderRepository poRepo;

//         public PurGrnResponse create(PurGrnRequest req) {
//                 if (req.getPurchaseOrderId() == null) {
//                         throw new IllegalArgumentException("purchaseOrderId required");
//                 }

//                 PurPurchaseOrder po = poRepo.findById(req.getPurchaseOrderId())
//                                 .orElseThrow(
//                                                 () -> new EntityNotFoundException("Purchase order not found: "
//                                                                 + req.getPurchaseOrderId()));

//                 PurGrn grn = PurGrn.builder()
//                                 .grnNumber(req.getGrnNumber())
//                                 .grnDate(req.getGrnDate())
//                                 .purchaseOrder(po)
//                                 .remark(req.getRemark())
//                                 .createdBy(req.getCreatedBy())
//                                 .createdAt(LocalDateTime.now())
//                                 .build();

//                 // defensive: ensure list initialized
//                 if (grn.getItems() == null)
//                         grn.setItems(new ArrayList<>());

//                 if (req.getItems() != null) {
//                         for (PurGrnItemRequest ir : req.getItems()) {
//                                 PurPurchaseOrderItem poItem = po.getItems().stream()
//                                                 .filter(i -> Objects.equals(i.getId(), ir.getPurchaseOrderItemId()))
//                                                 .findFirst()
//                                                 .orElseThrow(
//                                                                 () -> new EntityNotFoundException("PO item not found: "
//                                                                                 + ir.getPurchaseOrderItemId()));

//                                 PurGrnItem gi = PurGrnItem.builder()
//                                                 .purchaseOrderItem(poItem)
//                                                 .receivedQuantity(
//                                                                 Optional.ofNullable(ir.getReceivedQuantity())
//                                                                                 .orElse(java.math.BigDecimal.ZERO))
//                                                 .rate(ir.getRate())
//                                                 .unit(poItem.getUnit()) // Set the ProUnit object directly
//                                                 .build();

//                                 grn.addItem(gi);

//                                 // update PO item received quantity
//                                 java.math.BigDecimal prev = Optional.ofNullable(poItem.getReceivedQuantity())
//                                                 .orElse(java.math.BigDecimal.ZERO);
//                                 java.math.BigDecimal add = Optional.ofNullable(ir.getReceivedQuantity())
//                                                 .orElse(java.math.BigDecimal.ZERO);
//                                 poItem.setReceivedQuantity(prev.add(add));
//                         }
//                 }

//                 PurGrn saved = grnRepo.save(grn);
//                 // persist PO changes
//                 poRepo.save(po);

//                 return toResponse(saved);
//         }

//         @Transactional(readOnly = true)
//         public List<PurGrnResponse> listByOrder(Long orderId) {
//                 List<PurGrn> list = grnRepo.findByPurchaseOrderId(orderId);
//                 return list.stream().map(this::toResponse).collect(Collectors.toList());
//         }

//         @Transactional(readOnly = true)
//         public PurGrnResponse getById(Long id) {
//                 PurGrn g = grnRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("GRN not found: " + id));
//                 return toResponse(g);
//         }

//         private PurGrnItemResponse itemToResponse(PurGrnItem it) {
//                 return PurGrnItemResponse.builder()
//                                 .id(it.getId())
//                                 .purchaseOrderItemId(
//                                                 it.getPurchaseOrderItem() != null ? it.getPurchaseOrderItem().getId()
//                                                                 : null)
//                                 .itemName(it.getPurchaseOrderItem() != null
//                                                 && it.getPurchaseOrderItem().getItem() != null
//                                                                 ? it.getPurchaseOrderItem().getItem().getName()
//                                                                 : it.getPurchaseOrderItem() != null
//                                                                                 ? it.getPurchaseOrderItem()
//                                                                                                 .getDescription()
//                                                                                 : null)
//                                 .receivedQuantity(it.getReceivedQuantity())
//                                 .rate(it.getRate())
//                                 .unitId(it.getUnit() != null ? it.getUnit().getId() : null)
//                                 .build();
//         }

//         private PurGrnResponse toResponse(PurGrn g) {
//                 return PurGrnResponse.builder()
//                                 .id(g.getId())
//                                 .grnNumber(g.getGrnNumber())
//                                 .grnDate(g.getGrnDate())
//                                 .purchaseOrderId(g.getPurchaseOrder() != null ? g.getPurchaseOrder().getId() : null)
//                                 .remark(g.getRemark())
//                                 .createdBy(g.getCreatedBy())
//                                 .createdAt(g.getCreatedAt())
//                                 .items(Optional.ofNullable(g.getItems()).orElse(Collections.emptyList()).stream()
//                                                 .map(this::itemToResponse).collect(Collectors.toList()))
//                                 .build();
//         }
// }

package com.example.multi_tanent.purchases.service;

import com.example.multi_tanent.purchases.dto.*;
import com.example.multi_tanent.purchases.entity.*;
import com.example.multi_tanent.purchases.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for GRN (Goods Receipt Note) handling.
 *
 * - Prevents receiving more than ordered quantity on each PO line (server-side
 * validation).
 * - Uses a pessimistic lock when loading the PO (recommended) to avoid race
 * conditions where
 * two concurrent requests could both pass validation and over-receive.
 *
 * Make sure your PurPurchaseOrderRepository contains the
 * findByIdWithItemsForUpdate(...) method
 * (example repository snippet provided below).
 */
@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class PurGrnService {

        private final PurGrnRepository grnRepo;
        private final PurGrnItemRepository grnItemRepo;
        private final PurPurchaseOrderRepository poRepo;

        /**
         * Create a GRN for a purchase order.
         * Validates that receiving the requested quantities will NOT exceed the PO line
         * ordered quantity.
         *
         * @param req the GRN request DTO
         * @return created GRN response DTO
         */
        public PurGrnResponse create(PurGrnRequest req) {
                if (req.getPurchaseOrderId() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purchaseOrderId required");
                }

                // Recommended: fetch PO with items under a pessimistic write lock to avoid
                // concurrent over-receipts.
                // This requires a repository method: findByIdWithItemsForUpdate(Long id)
                PurPurchaseOrder po = poRepo.findByIdWithItemsForUpdate(req.getPurchaseOrderId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Purchase order not found: " + req.getPurchaseOrderId()));

                PurGrn grn = PurGrn.builder()
                                .grnNumber(req.getGrnNumber())
                                .grnDate(req.getGrnDate())
                                .purchaseOrder(po)
                                .remark(req.getRemark())
                                .createdBy(req.getCreatedBy())
                                .createdAt(LocalDateTime.now())
                                .build();

                // ensure list initialized
                if (grn.getItems() == null)
                        grn.setItems(new ArrayList<>());

                if (req.getItems() != null) {
                        for (PurGrnItemRequest ir : req.getItems()) {
                                // find the corresponding PO item
                                PurPurchaseOrderItem poItem = po.getItems().stream()
                                                .filter(i -> Objects.equals(i.getId(), ir.getPurchaseOrderItemId()))
                                                .findFirst()
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                "PO item not found: " + ir.getPurchaseOrderItemId()));

                                // incoming received quantity (default to zero if null)
                                java.math.BigDecimal incoming = Optional.ofNullable(ir.getReceivedQuantity())
                                                .orElse(java.math.BigDecimal.ZERO);

                                if (incoming.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                        "Received quantity must be > 0 for PO item " + poItem.getId());
                                }

                                java.math.BigDecimal prevReceived = Optional.ofNullable(poItem.getReceivedQuantity())
                                                .orElse(java.math.BigDecimal.ZERO);
                                java.math.BigDecimal newTotalReceived = prevReceived.add(incoming);

                                java.math.BigDecimal orderedQty = Optional.ofNullable(poItem.getQuantity())
                                                .orElse(java.math.BigDecimal.ZERO);

                                // validate does not exceed ordered qty
                                if (newTotalReceived.compareTo(orderedQty) > 0) {
                                        String msg = String.format(
                                                        "Cannot receive %s units for PO item %d — would exceed ordered quantity %s (already received %s).",
                                                        incoming.toPlainString(), poItem.getId(),
                                                        orderedQty.toPlainString(), prevReceived.toPlainString());
                                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
                                }

                                // create GRN item and attach
                                PurGrnItem gi = PurGrnItem.builder()
                                                .purchaseOrderItem(poItem)
                                                .receivedQuantity(incoming)
                                                .rate(ir.getRate())
                                                .unit(poItem.getUnit())
                                                .build();

                                grn.addItem(gi);

                                // update the PO item received quantity
                                poItem.setReceivedQuantity(newTotalReceived);
                        }
                }

                // Save GRN (cascades items) and persist PO updates
                PurGrn saved = grnRepo.save(grn);
                poRepo.save(po); // ensure PO changes persisted (received quantities updated)

                return toResponse(saved);
        }

        @Transactional(readOnly = true)
        public List<PurGrnResponse> listByOrder(Long orderId) {
                List<PurGrn> list = grnRepo.findByPurchaseOrderId(orderId);
                return list.stream().map(this::toResponse).collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public PurGrnResponse getById(Long id) {
                PurGrn g = grnRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("GRN not found: " + id));
                return toResponse(g);
        }

        /* ----- helpers to map entities to DTOs ----- */

        private PurGrnItemResponse itemToResponse(PurGrnItem it) {
                return PurGrnItemResponse.builder()
                                .id(it.getId())
                                .purchaseOrderItemId(
                                                it.getPurchaseOrderItem() != null ? it.getPurchaseOrderItem().getId()
                                                                : null)
                                .itemName(it.getPurchaseOrderItem() != null
                                                && it.getPurchaseOrderItem().getItem() != null
                                                                ? it.getPurchaseOrderItem().getItem().getName()
                                                                : it.getPurchaseOrderItem() != null
                                                                                ? it.getPurchaseOrderItem()
                                                                                                .getDescription()
                                                                                : null)
                                .receivedQuantity(it.getReceivedQuantity())
                                .rate(it.getRate())
                                .unitId(it.getUnit() != null ? it.getUnit().getId() : null)
                                .build();
        }

        private PurGrnResponse toResponse(PurGrn g) {
                return PurGrnResponse.builder()
                                .id(g.getId())
                                .grnNumber(g.getGrnNumber())
                                .grnDate(g.getGrnDate())
                                .purchaseOrderId(g.getPurchaseOrder() != null ? g.getPurchaseOrder().getId() : null)
                                .remark(g.getRemark())
                                .createdBy(g.getCreatedBy())
                                .createdAt(g.getCreatedAt())
                                .items(Optional.ofNullable(g.getItems()).orElse(Collections.emptyList()).stream()
                                                .map(this::itemToResponse).collect(Collectors.toList()))
                                .build();
        }
}
