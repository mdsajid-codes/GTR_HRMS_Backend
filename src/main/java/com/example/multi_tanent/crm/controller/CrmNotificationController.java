package com.example.multi_tanent.crm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.multi_tanent.crm.dto.CrmNotificationModuleSettingRequest;
import com.example.multi_tanent.crm.dto.CrmNotificationTemplateRequest;
import com.example.multi_tanent.crm.enums.NotificationModule;
import com.example.multi_tanent.crm.services.CrmNotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CrmNotificationController {

  private final CrmNotificationService service;

  /* --------- Templates (per sidebar module) --------- */

  @GetMapping("/modules/{module}/templates")
  public ResponseEntity<?> getAllByModule(@PathVariable NotificationModule module) {
    return ResponseEntity.ok(service.getAllByModule(module));
  }

  @GetMapping("/templates/{id}")
  public ResponseEntity<?> getOne(@PathVariable Long id) {
    return ResponseEntity.ok(service.getTemplate(id));
  }

  @PostMapping("/templates")
  public ResponseEntity<?> create(@Valid @RequestBody CrmNotificationTemplateRequest req) {
    return ResponseEntity.ok(service.createTemplate(req));
  }

  @PutMapping("/templates/{id}")
  public ResponseEntity<?> update(@PathVariable Long id,
                                  @Valid @RequestBody CrmNotificationTemplateRequest req) {
    return ResponseEntity.ok(service.updateTemplate(id, req));
  }

  @DeleteMapping("/templates/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id) {
    service.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  /* --------- Module Settings (Meta Response toggle, etc.) --------- */

  @GetMapping("/modules/{module}/settings")
  public ResponseEntity<?> getSetting(@PathVariable NotificationModule module) {
    return ResponseEntity.ok(service.getSetting(module));
  }

  @PostMapping("/modules/{module}/settings")
  public ResponseEntity<?> upsert(@PathVariable NotificationModule module,
                                  @RequestBody CrmNotificationModuleSettingRequest req) {
    req.setModule(module);
    return ResponseEntity.ok(service.upsertSetting(req));
  }
}

