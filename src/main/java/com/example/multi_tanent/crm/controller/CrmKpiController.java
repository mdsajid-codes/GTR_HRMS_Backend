package com.example.multi_tanent.crm.controller;



import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multi_tanent.crm.dto.CrmKpiRequest;
import com.example.multi_tanent.crm.dto.CrmKpiResponse;
import com.example.multi_tanent.crm.services.CrmKpiService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crm/kpis")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmKpiController {
  private final CrmKpiService kpiService;

  @GetMapping 
  public ResponseEntity<List<CrmKpiResponse>> getAll() { 
    return ResponseEntity.ok(kpiService.getAll()); 
  
  }

  @GetMapping("/{id}") 
  public ResponseEntity<CrmKpiResponse> getById(@PathVariable Long id) { 
    return ResponseEntity.ok(kpiService.getById(id)); 
  }

  @PostMapping 
  public ResponseEntity<CrmKpiResponse> create(@Valid @RequestBody CrmKpiRequest req) { 
    return ResponseEntity.ok(kpiService.create(req)); 
  }


  @PutMapping("/{id}") 
  public ResponseEntity<CrmKpiResponse> update(@PathVariable Long id, @Valid @RequestBody CrmKpiRequest req) { 
    return ResponseEntity.ok(kpiService.update(id, req)); 
  }


  @DeleteMapping("/{id}") 
  public ResponseEntity<Void> delete(@PathVariable Long id) { 
    kpiService.delete(id); 
    return ResponseEntity.noContent().build(); 
  }
}
