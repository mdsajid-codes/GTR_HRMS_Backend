// package com.example.multi_tanent.purchases.repository;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import com.example.multi_tanent.purchases.entity.PurGrn;
// import java.util.List;

// public interface PurGrnRepository extends JpaRepository<PurGrn, Long> {
//     Page<PurGrn> findByPurchaseOrder_Tenant_Id(Long tenantId, Pageable pageable);

//     List<PurGrn> findByPurchaseOrder_Id(Long purchaseOrderId);
// }

// package com.example.multi_tanent.purchases.repository;

// import org.springframework.data.jpa.repository.JpaRepository;
// import java.util.List;
// import com.example.multi_tanent.purchases.entity.PurGrn;

// public interface PurGrnRepository extends JpaRepository<PurGrn, Long> {
//     List<PurGrn> findByPurchaseOrderId(Long purchaseOrderId);
// }

// src/main/java/com/example/multi_tanent/purchases/repository/PurGrnRepository.java
package com.example.multi_tanent.purchases.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multi_tanent.purchases.entity.PurGrn;
import java.util.List;

public interface PurGrnRepository extends JpaRepository<PurGrn, Long> {
    List<PurGrn> findByPurchaseOrderId(Long purchaseOrderId);
}
