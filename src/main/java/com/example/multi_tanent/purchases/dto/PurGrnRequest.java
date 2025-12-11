// package com.example.multi_tanent.purchases.dto;

// import lombok.*;
// import java.time.LocalDate;
// import java.util.List;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnRequest {
//     private Long purchaseOrderId;
//     private String grnNumber;
//     private LocalDate grnDate;
//     private String remark;
//     private String createdBy;
//     private List<PurGrnItemRequest> items;
// }

// PurGrnRequest.java
// package com.example.multi_tanent.purchases.dto;

// import lombok.*;
// import java.time.LocalDate;
// import java.util.List;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnRequest {
//     private Long purchaseOrderId;
//     private String grnNumber;
//     private LocalDate grnDate;
//     private String remark;
//     private String createdBy;
//     private List<PurGrnItemRequest> items;
// }

// PurGrnItemRequest.java
// package com.example.multi_tanent.purchases.dto;

// import lombok.*;
// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;

// import jakarta.validation.constraints.NotNull;

// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class PurGrnRequest {
//     private Long purchaseOrderId;
//     @NotNull(message = "Purchase Order ID is required")
//     private String grnNumber;
//     private LocalDate grnDate;
//     private String remark;
//     private String createdBy;

//     @Builder.Default
//     private List<PurGrnItemRequest> items = new ArrayList<>();
// }

// src/main/java/com/example/multi_tanent/purchases/dto/PurGrnRequest.java
package com.example.multi_tanent.purchases.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurGrnRequest {
    private Long purchaseOrderId;
    private String grnNumber;
    private LocalDate grnDate;
    private String remark;
    private String createdBy;
    private List<PurGrnItemRequest> items;
}
