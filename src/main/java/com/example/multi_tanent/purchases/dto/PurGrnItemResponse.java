// package com.example.multi_tanent.purchases.dto;

// import lombok.*;

// import java.math.BigDecimal;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnItemResponse {
//     private Long id;
//     private Long purchaseOrderItemId;a
//     private String itemName;
//     private BigDecimal receivedQuantity;
//     private BigDecimal rate;
//     private Long unitId;
//     private String unitName;
// }

// PurGrnItemResponse.java
// package com.example.multi_tanent.purchases.dto;

// import lombok.*;
// import java.math.BigDecimal;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnItemResponse {
//     private Long id;
//     private Long purchaseOrderItemId;
//     private String itemName;
//     private BigDecimal receivedQuantity;
//     private BigDecimal rate;
//     private Long unitId;
// }
// src/main/java/com/example/multi_tanent/purchases/dto/PurGrnItemResponse.java
package com.example.multi_tanent.purchases.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurGrnItemResponse {
    private Long id;
    private Long purchaseOrderItemId;
    private String itemName;
    private BigDecimal receivedQuantity;
    private BigDecimal rate;
    private Long unitId;
}
