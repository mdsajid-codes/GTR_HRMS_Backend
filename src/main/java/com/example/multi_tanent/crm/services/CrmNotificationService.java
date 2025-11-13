package com.example.multi_tanent.crm.services;


import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmNotificationModuleSettingRequest;
import com.example.multi_tanent.crm.dto.CrmNotificationModuleSettingResponse;
import com.example.multi_tanent.crm.dto.CrmNotificationTemplateRequest;
import com.example.multi_tanent.crm.dto.CrmNotificationTemplateResponse;
import com.example.multi_tanent.crm.entity.CrmNotificationModuleSetting;
import com.example.multi_tanent.crm.entity.CrmNotificationTemplate;
import com.example.multi_tanent.crm.entity.CrmNotificationTemplateEmployee;
import com.example.multi_tanent.crm.enums.NotificationModule;
import com.example.multi_tanent.crm.repository.CrmNotificationModuleSettingRepository;
import com.example.multi_tanent.crm.repository.CrmNotificationTemplateEmployeeRepository;
import com.example.multi_tanent.crm.repository.CrmNotificationTemplateRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
//import com.example.multi_tanent.crm.repository.EmployeeRepository; // adjust package
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmNotificationService {

  private final CrmNotificationTemplateRepository templateRepo;
  private final CrmNotificationTemplateEmployeeRepository recipientRepo;
  private final CrmNotificationModuleSettingRepository settingRepo;
  private final EmployeeRepository employeeRepo;
  private final TenantRepository tenantRepo;

  private Tenant currentTenant() {
    String tenantId = TenantContext.getTenantId();
    return tenantRepo.findFirstByOrderByIdAsc()
        .orElseThrow(() -> new IllegalStateException("Tenant not found in current DB for tenantId: " + tenantId));
  }

  /* -------------------- Templates -------------------- */

  public CrmNotificationTemplateResponse createTemplate(CrmNotificationTemplateRequest req) {
    Tenant t = currentTenant();

    String messageType = (req.getMessageType() == null || req.getMessageType().isBlank()) ? null : req.getMessageType().trim();
    boolean exists = templateRepo.existsByTenantIdAndModuleAndEventAndMessageTypeIgnoreCase(
        t.getId(), req.getModule(), req.getEvent(), messageType);
    if (exists) throw new IllegalArgumentException("Template already exists for this module/event/type");

    CrmNotificationTemplate tpl = CrmNotificationTemplate.builder()
        .tenant(t)
        .module(req.getModule())
        .event(req.getEvent())
        .messageType(messageType)
        .messageBody(req.getMessageBody())
        .bell(req.isBell())
        .email(req.isEmail())
        .whatsapp(req.isWhatsapp())
        .sms(req.isSms())
        .telegram(req.isTelegram())
        .screenPopup(req.isScreenPopup())
        .providerTemplateId(req.getProviderTemplateId())
        .active(true)
        .build();

    // recipients
    if (req.getEmployeeIds() != null && !req.getEmployeeIds().isEmpty()) {
      req.getEmployeeIds().forEach(eid -> {
        var emp = employeeRepo.findById(eid)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + eid));
        tpl.getRecipients().add(CrmNotificationTemplateEmployee.builder()
            .tenant(t).template(tpl).employee(emp).build());
      });
    }

    return toResponse(templateRepo.save(tpl));
  }

  @Transactional(readOnly = true, transactionManager = "tenantTx")
  public List<CrmNotificationTemplateResponse> getAllByModule(NotificationModule module) {
    Tenant t = currentTenant();
    return templateRepo.findByTenantIdAndModuleOrderByEventAscIdAsc(t.getId(), module)
        .stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true, transactionManager = "tenantTx")
  public CrmNotificationTemplateResponse getTemplate(Long id) {
    Tenant t = currentTenant();
    CrmNotificationTemplate tpl = templateRepo.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    return toResponse(tpl);
  }

  public CrmNotificationTemplateResponse updateTemplate(Long id, CrmNotificationTemplateRequest req) {
    Tenant t = currentTenant();
    CrmNotificationTemplate tpl = templateRepo.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));

    tpl.setMessageBody(req.getMessageBody());
    tpl.setBell(req.isBell());
    tpl.setEmail(req.isEmail());
    tpl.setWhatsapp(req.isWhatsapp());
    tpl.setSms(req.isSms());
    tpl.setTelegram(req.isTelegram());
    tpl.setScreenPopup(req.isScreenPopup());
    tpl.setProviderTemplateId(req.getProviderTemplateId());

    // update recipients (replace)
    tpl.getRecipients().clear();
    if (req.getEmployeeIds() != null) {
      req.getEmployeeIds().forEach(eid -> {
        var emp = employeeRepo.findById(eid)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + eid));
        tpl.getRecipients().add(CrmNotificationTemplateEmployee.builder()
            .tenant(t).template(tpl).employee(emp).build());
      });
    }

    return toResponse(templateRepo.save(tpl));
  }

  public void deleteTemplate(Long id) {
    Tenant t = currentTenant();
    CrmNotificationTemplate tpl = templateRepo.findByIdAndTenantId(id, t.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    templateRepo.delete(tpl);
  }

  /* -------------------- Module Settings (Meta Response) -------------------- */

  public CrmNotificationModuleSettingResponse upsertSetting(CrmNotificationModuleSettingRequest req) {
    Tenant t = currentTenant();
    CrmNotificationModuleSetting setting = settingRepo.findByTenantIdAndModule(t.getId(), req.getModule())
        .orElse(CrmNotificationModuleSetting.builder().tenant(t).module(req.getModule()).build());

    setting.setMetaResponseEnabled(req.isMetaResponseEnabled());
    var saved = settingRepo.save(setting);
    return CrmNotificationModuleSettingResponse.builder()
        .id(saved.getId()).module(saved.getModule())
        .metaResponseEnabled(saved.isMetaResponseEnabled()).build();
  }

  @Transactional(readOnly = true, transactionManager = "tenantTx")
  public CrmNotificationModuleSettingResponse getSetting(NotificationModule module) {
    Tenant t = currentTenant();
    CrmNotificationModuleSetting s = settingRepo.findByTenantIdAndModule(t.getId(), module)
        .orElse(CrmNotificationModuleSetting.builder().tenant(t).module(module).metaResponseEnabled(false).build());
    return CrmNotificationModuleSettingResponse.builder()
        .id(s.getId()).module(s.getModule()).metaResponseEnabled(s.isMetaResponseEnabled()).build();
  }

  /* -------------------- Mapping -------------------- */

  private CrmNotificationTemplateResponse toResponse(CrmNotificationTemplate e) {
    return CrmNotificationTemplateResponse.builder()
        .id(e.getId())
        .module(e.getModule())
        .event(e.getEvent())
        .messageType(e.getMessageType())
        .messageBody(e.getMessageBody())
        .bell(e.isBell())
        .email(e.isEmail())
        .whatsapp(e.isWhatsapp())
        .sms(e.isSms())
        .telegram(e.isTelegram())
        .screenPopup(e.isScreenPopup())
        .providerTemplateId(e.getProviderTemplateId())
        .active(e.isActive())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .employeeIds(e.getRecipients().stream().map(r -> r.getEmployee().getId()).toList())
        .build();
  }
}
