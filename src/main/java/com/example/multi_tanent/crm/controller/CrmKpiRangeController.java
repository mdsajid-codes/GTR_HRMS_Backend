package com.example.multi_tanent.crm.controller;



import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multi_tanent.crm.dto.CrmKpiRangeRequest;
import com.example.multi_tanent.crm.entity.CrmKpiRange;
import com.example.multi_tanent.crm.services.CrmKpiRangeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/crm/kpis/{kpiId}/ranges")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmKpiRangeController {
  private final CrmKpiRangeService kpiRangeService;

  @GetMapping 
  public ResponseEntity<List<CrmKpiRange>> getAll(@PathVariable Long kpiId) 
  { 
    return ResponseEntity.ok(kpiRangeService.getAllByKpi(kpiId));
  }

  @GetMapping("/{id}") 
  public ResponseEntity<CrmKpiRange> getById(@PathVariable Long kpiId, @PathVariable Long id) { return ResponseEntity.ok(kpiRangeService.getById(kpiId, id)); 
  }

  @PostMapping 
  public ResponseEntity<CrmKpiRange> create(@PathVariable Long kpiId, @Valid @RequestBody CrmKpiRangeRequest req) { 
    req.setKpiId(kpiId); return ResponseEntity.ok(kpiRangeService.create(req)); 
  }

  @PutMapping("/{id}") 
  public ResponseEntity<CrmKpiRange> update(@PathVariable Long kpiId, @PathVariable Long id, @Valid @RequestBody CrmKpiRangeRequest req) { 
    return ResponseEntity.ok(kpiRangeService.update(kpiId, id, req)); 
  }

  @DeleteMapping("/{id}") 
  public ResponseEntity<Void> delete(@PathVariable Long kpiId, @PathVariable Long id) { 
    kpiRangeService.delete(kpiId, id); return ResponseEntity.noContent().build(); 
  }
}
