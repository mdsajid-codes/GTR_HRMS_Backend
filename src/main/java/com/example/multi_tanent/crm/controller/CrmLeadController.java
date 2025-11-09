// package com.example.multi_tanent.crm.controller;

// import com.example.multi_tanent.crm.dto.CrmLeadRequest;
// import com.example.multi_tanent.crm.dto.CrmLeadResponse;
// import com.example.multi_tanent.crm.services.CrmLeadService;
// import jakarta.validation.Valid;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.data.web.PageableDefault;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/crm/leads")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class CrmLeadController {

//     private final CrmLeadService leadService;

//     @PostMapping
//     public ResponseEntity<CrmLeadResponse> createLead(@Valid @RequestBody CrmLeadRequest request) {
//         return new ResponseEntity<>(leadService.createLead(request), HttpStatus.CREATED);
//     }

//     @GetMapping
//     public Page<CrmLeadResponse> getAllLeads(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
//         return leadService.getAllLeads(pageable);
//     }

//     @GetMapping("/{id}")
//     public ResponseEntity<CrmLeadResponse> getLeadById(@PathVariable Long id) {
//         return ResponseEntity.ok(leadService.getLeadById(id));
//     }

//     @PutMapping("/{id}")
//     public ResponseEntity<CrmLeadResponse> updateLead(@PathVariable Long id, @Valid @RequestBody CrmLeadRequest request) {
//         return ResponseEntity.ok(leadService.updateLead(id, request));
//     }

//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
//         leadService.deleteLead(id);
//         return ResponseEntity.noContent().build();
//     }
// }

package com.example.multi_tanent.crm.controller;

import com.example.multi_tanent.crm.dto.CrmLeadRequest;
import com.example.multi_tanent.crm.dto.CrmLeadResponse;
import com.example.multi_tanent.crm.services.CrmLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/leads")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmLeadController {

    private final CrmLeadService service;

    @PostMapping
    public ResponseEntity<CrmLeadResponse> create(@Valid @RequestBody CrmLeadRequest req) {
        return new ResponseEntity<>(service.create(req), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<CrmLeadResponse> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.list(pageable);
    }

    @GetMapping("/{id}")
    public CrmLeadResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public CrmLeadResponse update(@PathVariable Long id, @Valid @RequestBody CrmLeadRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
