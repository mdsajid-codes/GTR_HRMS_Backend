// package com.example.multi_tanent.purchases.dto;

// import lombok.*;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnItemRequest {
//     private Long purchaseOrderItemId; // must match PO line id
//     private java.math.BigDecimal receivedQuantity;
//     private java.math.BigDecimal rate;
//     private Long unitId;
// }

// PurGrnRequest.java

// PurGrnItemRequest.java
// package com.example.multi_tanent.purchases.dto;

// import lombok.*;
// import java.math.BigDecimal;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnItemRequest {
//     private Long purchaseOrderItemId;
//     private BigDecimal receivedQuantity;
//     private BigDecimal rate;
//     private Long unitId;
// }

// PurGrnResponse.java

// PurGrnItemResponse.java

// src/main/java/com/example/multi_tanent/purchases/dto/PurGrnItemRequest.java
package com.example.multi_tanent.purchases.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurGrnItemRequest {
    private Long purchaseOrderItemId;
    private BigDecimal receivedQuantity;
    private BigDecimal rate;
    private Long unitId;
}
