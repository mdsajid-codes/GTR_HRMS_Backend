// package com.example.multi_tanent.purchases.controller;

// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.service.PurGrnService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.*;
// import org.springframework.http.*;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/purchase/grns")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class PurGrnController {

//     private final PurGrnService service;

//     @PostMapping
//     public ResponseEntity<PurGrnResponse> create(@RequestBody PurGrnRequest req) {
//         PurGrnResponse resp = service.create(req);
//         return ResponseEntity.status(HttpStatus.CREATED).body(resp);
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<PurGrnResponse> getById(@PathVariable Long id) {
//         return ResponseEntity.ok(service.getById(id));
//     }

//     @GetMapping("/by-order/{orderId}")
//     public ResponseEntity<List<PurGrnResponse>> listByOrder(@PathVariable Long orderId) {
//         return ResponseEntity.ok(service.listByOrder(orderId));
//     }

//     @GetMapping
//     public ResponseEntity<Page<PurGrnResponse>> list(
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "20") int size,
//             @RequestParam(defaultValue = "createdAt,desc") String sort) {
//         Sort s = Sort.by(Sort.Direction.DESC, "createdAt");
//         try {
//             String[] sp = sort.split(",");
//             if (sp.length == 2) {
//                 s = Sort.by(Sort.Direction.fromString(sp[1]), sp[0]);
//             }
//         } catch (Exception ignored) {
//         }
//         Pageable p = PageRequest.of(page, size, s);
//         return ResponseEntity.ok(service.list(p));
//     }
// }

// package com.example.multi_tanent.purchases.controller;

// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
// import com.example.multi_tanent.purchases.dto.*;
// import com.example.multi_tanent.purchases.service.PurGrnService;

// import java.util.List;

// @RestController
// @RequestMapping("/api/purchase/grns")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class PurGrnController {

//     private final PurGrnService grnService;

//     @PostMapping
//     public ResponseEntity<PurGrnResponse> create(@RequestBody PurGrnRequest req) {
//         PurGrnResponse resp = grnService.create(req);
//         return ResponseEntity.status(201).body(resp);
//     }

//     @GetMapping("/by-order/{orderId}")
//     public ResponseEntity<List<PurGrnResponse>> listByOrder(@PathVariable Long orderId) {
//         return ResponseEntity.ok(grnService.listByOrder(orderId));
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<PurGrnResponse> getById(@PathVariable Long id) {
//         return ResponseEntity.ok(grnService.getById(id));
//     }

//     // optional: implement print/pdf endpoint later
//     @GetMapping("/{id}/print")
//     public ResponseEntity<PurGrnResponse> print(@PathVariable Long id) {
//         PurGrnResponse resp = grnService.getById(id);
//         // for now return the response; frontend can render printable HTML
//         return ResponseEntity.ok(resp);
//     }
// }

// src/main/java/com/example/multi_tanent/purchases/controller/PurGrnController.java
package com.example.multi_tanent.purchases.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.multi_tanent.purchases.dto.*;
import com.example.multi_tanent.purchases.service.PurGrnService;

import java.util.List;

@RestController
@RequestMapping("/api/purchase/grns")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PurGrnController {

    private final PurGrnService grnService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PurGrnResponse> create(@RequestBody PurGrnRequest req) {
        PurGrnResponse resp = grnService.create(req);
        return ResponseEntity.status(201).body(resp);
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PurGrnResponse>> listByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(grnService.listByOrder(orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurGrnResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.getById(id));
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<PurGrnResponse> print(@PathVariable Long id) {
        PurGrnResponse resp = grnService.getById(id);
        return ResponseEntity.ok(resp);
    }
}
