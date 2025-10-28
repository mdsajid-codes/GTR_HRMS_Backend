package com.example.multi_tanent.crm.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multi_tanent.crm.dto.CrmIndustryDto;
import com.example.multi_tanent.crm.entity.CrmIndustry;
import com.example.multi_tanent.crm.services.CrmIndustryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settings/industries")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmIndustryController {

  private final CrmIndustryService industryService;

  @GetMapping
  public ResponseEntity<List<CrmIndustry>> list() {
    return ResponseEntity.ok(industryService.getAllIndustries());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CrmIndustry> getById(@PathVariable Long id) {
    return ResponseEntity.ok(industryService.getIndustryById(id));
  }

  @PostMapping
  public ResponseEntity<CrmIndustry> create(@Valid @RequestBody CrmIndustryDto req) {
    return ResponseEntity.ok(industryService.create(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CrmIndustry> update(@PathVariable Long id,
                                         @Valid @RequestBody CrmIndustryDto req) {
    return ResponseEntity.ok(industryService.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    industryService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
