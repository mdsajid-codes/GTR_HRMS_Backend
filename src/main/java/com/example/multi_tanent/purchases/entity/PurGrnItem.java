// package com.example.multi_tanent.purchases.entity;

// import jakarta.persistence.*;
// import com.example.multi_tanent.production.entity.ProUnit;
// import lombok.*;
// import java.math.BigDecimal;
// import java.util.Objects;

// @Entity
// @Table(name = "pur_grn_item")
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnItem {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     // owning GRN
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "grn_id", nullable = false)
//     private PurGrn grn;

//     // optional link to PO item for convenience
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "purchase_order_item_id")
//     private PurPurchaseOrderItem purchaseOrderItem;

//     @Column(name = "received_quantity", precision = 19, scale = 6)
//     private BigDecimal receivedQuantity;

//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "unit_id")
//     private ProUnit unit;

//     @Column(name = "rate", precision = 19, scale = 4)
//     private BigDecimal rate;

//     @Override
//     public boolean equals(Object o) {
//         if (this == o)
//             return true;
//         if (!(o instanceof PurGrnItem))
//             return false;
//         PurGrnItem that = (PurGrnItem) o;
//         return id != null && id.equals(that.id);
//     }

//     @Override
//     public int hashCode() {
//         return Objects.hashCode(id);
//     }
// }

// 

// src/main/java/com/example/multi_tanent/purchases/entity/PurGrnItem.java
package com.example.multi_tanent.purchases.entity;

import com.example.multi_tanent.production.entity.ProUnit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "pur_grn_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurGrnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // owning GRN
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private PurGrn grn;

    // optional link to PO item for convenience
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id")
    private PurPurchaseOrderItem purchaseOrderItem;

    @Column(name = "received_quantity", precision = 19, scale = 6)
    private BigDecimal receivedQuantity = BigDecimal.ZERO;

    @Column(name = "rate", precision = 19, scale = 4)
    private BigDecimal rate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private ProUnit unit;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PurGrnItem))
            return false;
        PurGrnItem that = (PurGrnItem) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
