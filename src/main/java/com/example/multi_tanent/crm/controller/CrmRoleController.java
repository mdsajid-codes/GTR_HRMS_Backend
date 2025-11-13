// package com.example.multi_tanent.crm.controller;


// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import com.example.multi_tanent.crm.dto.CrmRoleRequest;
// import com.example.multi_tanent.crm.dto.CrmRoleResponse;
// import com.example.multi_tanent.crm.services.CrmRoleService;

// import java.util.List;

// @RestController
// @RequestMapping("/api/crm/roles")
// @CrossOrigin(origins = "*")
// @RequiredArgsConstructor
// public class CrmRoleController {

//     private final CrmRoleService service;

//     @GetMapping
//     public ResponseEntity<List<CrmRoleResponse>> getAll() {
//         return ResponseEntity.ok(service.getAll());
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<CrmRoleResponse> getById(@PathVariable Long id) {
//         return ResponseEntity.ok(service.getById(id));
//     }

//     @PostMapping
//     public ResponseEntity<CrmRoleResponse> create(@Valid @RequestBody CrmRoleRequest req) {
//         return ResponseEntity.ok(service.create(req));
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<CrmRoleResponse> update(@PathVariable Long id,
//                                                @Valid @RequestBody CrmRoleRequest req) {
//         return ResponseEntity.ok(service.update(id, req));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(@PathVariable Long id) {
//         service.delete(id);
//         return ResponseEntity.noContent().build();
//     }
// }
