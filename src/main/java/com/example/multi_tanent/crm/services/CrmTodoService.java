// package com.example.multi_tanent.crm.services;



// import com.example.multi_tanent.config.TenantContext;
// import com.example.multi_tanent.crm.dto.*;
// import com.example.multi_tanent.crm.entity.*;
// import com.example.multi_tanent.crm.enums.TaskSubject;
// import com.example.multi_tanent.crm.repository.*;
// import com.example.multi_tanent.pos.repository.TenantRepository;
// import com.example.multi_tanent.spersusers.enitity.Employee;
// import com.example.multi_tanent.spersusers.enitity.Tenant;
// import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository; // Assuming this is the correct EmployeeRepository
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.*;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.time.ZoneOffset;
// import java.time.format.DateTimeFormatter;
// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// @Transactional("tenantTx")
// public class CrmTodoService {

//     private final CrmCrmCrmTodoItemRepository todoRepo;
//     private final CrmCrmTodoLabelRepository labelRepo;
//     private final TenantRepository tenantRepo;
//     private final EmployeeRepository employeeRepo;
//     private final ContactRepository contactRepo; // you already have this

//     /* ---------------------- tenant ---------------------- */

//     private Tenant currentTenant() {
//         // Use your own pattern (name or id). Below we follow your CrmTaskService example:
//         return tenantRepo.findFirstByOrderByIdAsc()
//                 .orElseThrow(() -> new IllegalStateException("Tenant not resolved: " + TenantContext.getTenantId()));
//     }

//     /* ---------------------- CRUD ------------------------ */

//     public CrmCrmTodoResponse create(CrmCrmTodoRequest req) {
//         Tenant t = currentTenant();

//         CrmCrmTodoItem e = new CrmCrmTodoItem();
//         e.setTenant(t);
//         apply(req, e);

//         return toResponse(todoRepo.save(e));
//     }

//     public CrmCrmTodoResponse update(Long id, CrmCrmTodoRequest req) {
//         Tenant t = currentTenant();
//         CrmCrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));

//         apply(req, e);

//         return toResponse(todoRepo.save(e));
//     }

//     public void delete(Long id) {
//         Tenant t = currentTenant();
//         CrmCrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
//         todoRepo.delete(e);
//     }

//     @Transactional(readOnly = true)
//     public CrmCrmTodoResponse get(Long id) {
//         Tenant t = currentTenant();
//         CrmCrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
//         return toResponse(e);
//     }

//     /* ---------------------- Search + counts ------------- */

//     @Transactional(readOnly = true)
//     public Page<CrmCrmTodoResponse> search(CrmCrmTodoFilterRequest f, Pageable pageable) {
//         Tenant t = currentTenant();
//         Specification<CrmCrmTodoItem> spec = specForTenant(t.getId())
//                 .and(specDateRange(f))
//                 .and(specPriority(f))
//                 .and(specStatus(f))
//                 .and(specSubject(f))
//                 .and(specLabel(f.getLabelId()))
//                 .and(specEmployees(f.getEmployeeIds()));

//         Pageable sorted = ensureSort(pageable, f);
//         return todoRepo.findAll(spec, sorted).map(this::toResponse);
//     }

//     @Transactional(readOnly = true)
//     public List<CrmTodoSubjectCount> subjectCounts() {
//         Tenant t = currentTenant();
//         List<Object[]> rows = todoRepo.countBySubject(t.getId());
//         return rows.stream()
//                 .map(r -> CrmTodoSubjectCount.builder()
//                         .subject((TaskSubject) r[0])
//                         .count(((Number) r[1]).longValue())
//                         .build())
//                 .collect(Collectors.toList());
//     }

//     /* ---------------------- Labels ---------------------- */

//     public CrmTodoLabelResponse createLabel(CrmTodoLabelRequest req) {
//         Tenant t = currentTenant();
//         CrmTodoLabel l = CrmTodoLabel.builder()
//                 .tenant(t)
//                 .name(req.getName().trim())
//                 .colorHex(normalizeHex(req.getColorHex()))
//                 .starred(req.isStarred())
//                 .build();
//         return toLabelResponse(labelRepo.save(l));
//     }

//     public CrmTodoLabelResponse updateLabel(Long id, CrmTodoLabelRequest req) {
//         Tenant t = currentTenant();
//         CrmTodoLabel l = labelRepo.findByIdAndTenantId(id, t.getId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
//         l.setName(req.getName().trim());
//         l.setColorHex(normalizeHex(req.getColorHex()));
//         l.setStarred(req.isStarred());
//         return toLabelResponse(labelRepo.save(l));
//     }

//     public void deleteLabel(Long id) {
//         Tenant t = currentTenant();
//         CrmTodoLabel l = labelRepo.findByIdAndTenantId(id, t.getId())
//                 .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
//         labelRepo.delete(l);
//     }

//     @Transactional(readOnly = true)
//     public List<CrmTodoLabelResponse> getLabels() {
//         Tenant t = currentTenant();
//         return labelRepo.findByTenantIdOrderByNameAsc(t.getId())
//                 .stream().map(this::toLabelResponse).collect(Collectors.toList());
//     }

//     /* ---------------------- mapping --------------------- */

//     private void apply(CrmCrmTodoRequest req, CrmCrmTodoItem e) {
//         if (req.getSubject() != null) e.setSubject(req.getSubject());
//         e.setDescription(req.getDescription());
//         e.setDueDate(req.getDueDate());
//         e.setFromTime(req.getFromTime());
//         e.setToTime(req.getToTime());
//         if (req.getStatus() != null) e.setStatus(req.getStatus());
//         if (req.getPriority() != null) e.setPriority(req.getPriority());
//         e.setCustomerContactName(req.getCustomerContactName());

//         // Primary assignee
//         if (req.getAssignedToEmployeeId() != null) {
//             Employee assignee = employeeRepo.findById(req.getAssignedToEmployeeId())
//                     .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + req.getAssignedToEmployeeId()));
//             e.setAssignedTo(assignee);
//         } else {
//             e.setAssignedTo(null);
//         }

//         // Participants
//         if (req.getEmployeeIds() != null) {
//             e.getEmployees().clear();
//             e.getEmployees().addAll(new LinkedHashSet<>(employeeRepo.findAllById(req.getEmployeeIds())));
//         }

//         // Contacts
//         if (req.getContactIds() != null) {
//             e.getContacts().clear();
//             e.getContacts().addAll(new LinkedHashSet<>(contactRepo.findAllById(req.getContactIds())));
//         }

//         // Labels
//         if (req.getLabelIds() != null) {
//             Tenant t = e.getTenant();
//             List<CrmTodoLabel> labels = labelRepo.findAllById(req.getLabelIds())
//                     .stream().filter(l -> l.getTenant().getId().equals(t.getId())).toList();
//             e.getLabels().clear();
//             e.getLabels().addAll(labels);
//         }
//     }

//     private CrmCrmTodoResponse toResponse(CrmCrmTodoItem e) {
//         DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);
//         return CrmCrmTodoResponse.builder()
//                 .id(e.getId())
//                 .subject(e.getSubject())
//                 .description(e.getDescription())
//                 .dueDate(e.getDueDate())
//                 .fromTime(e.getFromTime())
//                 .toTime(e.getToTime())
//                 .status(e.getStatus())
//                 .priority(e.getPriority())
//                 .customerContactName(e.getCustomerContactName())
//                 .assignedToId(e.getAssignedTo() != null ? e.getAssignedTo().getId() : null)
//                 .assignedToName(e.getAssignedTo() != null
//                         ? (safe(e.getAssignedTo().getFirstName()) + " " + safe(e.getAssignedTo().getLastName())).trim()
//                         : null)
//                 .employees(e.getEmployees().stream()
//                         .map(emp -> new SimpleIdNameDto(emp.getId(), (safe(emp.getFirstName()) + " " + safe(emp.getLastName())).trim()))
//                         .collect(Collectors.toList()))
//                 .contacts(e.getContacts().stream()
//                         .map(c -> new SimpleIdNameDto(c.getId(), safe(c.getFullName())))
//                         .collect(Collectors.toList()))
//                 .labels(e.getLabels().stream()
//                         .map(l -> new SimpleIdNameDto(l.getId(), l.getName()))
//                         .collect(Collectors.toList()))
//                 .createdAt(e.getCreatedAt() != null ? ISO.format(e.getCreatedAt()) : null)
//                 .updatedAt(e.getUpdatedAt() != null ? ISO.format(e.getUpdatedAt()) : null)
//                 .build();
//     }

//     private CrmTodoLabelResponse toLabelResponse(CrmTodoLabel l) {
//         return CrmTodoLabelResponse.builder()
//                 .id(l.getId())
//                 .name(l.getName())
//                 .colorHex(l.getColorHex())
//                 .starred(l.isStarred())
//                 .build();
//     }

//     private String safe(String s) { return s == null ? "" : s; }
//     private String normalizeHex(String hex) {
//         if (hex == null) return null;
//         String h = hex.trim();
//         if (h.isEmpty()) return null;
//         return h.startsWith("#") ? h : "#" + h;
//     }

//     /* ------------------- Specifications ---------------- */

//     private Specification<CrmCrmTodoItem> specForTenant(Long tenantId) {
//         return (root, q, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
//     }

//     private Specification<CrmCrmTodoItem> specDateRange(CrmCrmTodoFilterRequest f) {
//         if (f == null || f.getDateMode() == null) return null;

//         LocalDate from;
//         LocalDate to;

//         switch (f.getDateMode()) {
//             case TODAY -> {
//                 from = LocalDate.now();
//                 to = from;
//             }
//             case NEXT_DAY -> {
//                 from = LocalDate.now().plusDays(1);
//                 to = from;
//             }
//             case NEXT_WEEK -> {
//                 from = LocalDate.now();
//                 to = LocalDate.now().plusWeeks(1);
//             }
//             case LAST_2_WEEKS -> {
//                 from = LocalDate.now().minusWeeks(2);
//                 to = LocalDate.now();
//             }
//             case CUSTOM -> {
//                 from = f.getFromDate();
//                 to = f.getToDate();
//             }
//             default -> { return null; }
//         }

//         if (from == null && to == null) return null;

//         return (root, q, cb) -> {
//             if (from != null && to != null) {
//                 return cb.between(root.get("dueDate"), from, to);
//             } else if (from != null) {
//                 return cb.greaterThanOrEqualTo(root.get("dueDate"), from);
//             } else {
//                 return cb.lessThanOrEqualTo(root.get("dueDate"), to);
//             }
//         };
//     }

//     private Specification<CrmCrmTodoItem> specPriority(CrmCrmTodoFilterRequest f) {
//         if (f == null || f.getPriority() == null) return null;
//         return (root, q, cb) -> cb.equal(root.get("priority"), f.getPriority());
//     }

//     private Specification<CrmCrmTodoItem> specStatus(CrmCrmTodoFilterRequest f) {
//         if (f == null || f.getStatus() == null) return null;
//         return (root, q, cb) -> cb.equal(root.get("status"), f.getStatus());
//     }

//     private Specification<CrmCrmTodoItem> specSubject(CrmCrmTodoFilterRequest f) {
//         if (f == null || f.getSubject() == null) return null;
//         return (root, q, cb) -> cb.equal(root.get("subject"), f.getSubject());
//     }

//     private Specification<CrmCrmTodoItem> specLabel(Long labelId) {
//         if (labelId == null) return null;
//         return (root, q, cb) -> {
//             q.distinct(true);
//             return cb.equal(root.join("labels").get("id"), labelId);
//         };
//     }

//     private Specification<CrmCrmTodoItem> specEmployees(Set<Long> employeeIds) {
//         if (employeeIds == null || employeeIds.isEmpty()) return null;
//         return (root, q, cb) -> {
//             q.distinct(true);
//             return root.join("employees").get("id").in(employeeIds);
//         };
//     }

//     private Pageable ensureSort(Pageable pageable, CrmCrmTodoFilterRequest f) {
//         if (pageable.getSort().isSorted()) return pageable;
//         Sort sort;
//         CrmCrmTodoFilterRequest.SortBy by = f != null ? f.getSortBy() : CrmCrmTodoFilterRequest.SortBy.CREATED_DATE;
//         CrmCrmTodoFilterRequest.Direction dir = f != null ? f.getDirection() : CrmCrmTodoFilterRequest.Direction.DESC;

//         String column = switch (by) {
//             case DUE_DATE -> "dueDate";
//             case UPDATED_DATE -> "updatedAt";
//             default -> "createdAt";
//         };

//         sort = (dir == CrmCrmTodoFilterRequest.Direction.ASC) ? Sort.by(column).ascending() : Sort.by(column).descending();
//         return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
//     }
// }
package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.*;
import com.example.multi_tanent.crm.entity.CrmTodoItem;
import com.example.multi_tanent.crm.entity.CrmTodoLabel;

import com.example.multi_tanent.crm.enums.TaskSubject;

import com.example.multi_tanent.crm.repository.CrmTodoItemRepository;
import com.example.multi_tanent.crm.repository.CrmTodoLabelRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.crm.repository.ContactRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmTodoService {

    private final CrmTodoItemRepository todoRepo;
    private final CrmTodoLabelRepository labelRepo;
    private final TenantRepository tenantRepo;
    private final EmployeeRepository employeeRepo;
    private final ContactRepository contactRepo;

    /* ---------------- tenant ---------------- */

    private Tenant currentTenant() {
        // Keep consistent with your other services (you previously used findFirstByOrderByIdAsc()).
        return tenantRepo.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant not resolved: " + TenantContext.getTenantId()));
    }

    /* ---------------- CRUD ------------------ */

    public CrmTodoResponse create(CrmTodoRequest req) {
        Tenant t = currentTenant();
        CrmTodoItem e = new CrmTodoItem();
        e.setTenant(t);
        apply(req, e);
        return toResponse(todoRepo.save(e));
    }

    public CrmTodoResponse update(Long id, CrmTodoRequest req) {
        Tenant t = currentTenant();
        CrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
        apply(req, e);
        return toResponse(todoRepo.save(e));
    }

    public void delete(Long id) {
        Tenant t = currentTenant();
        CrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
        todoRepo.delete(e);
    }

    @Transactional(readOnly = true)
    public CrmTodoResponse get(Long id) {
        Tenant t = currentTenant();
        CrmTodoItem e = todoRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Todo not found: " + id));
        return toResponse(e);
    }

    /* -------- search + counts -------- */

    @Transactional(readOnly = true)
    public Page<CrmTodoResponse> search(CrmTodoFilterRequest f, Pageable pageable) {
        Tenant t = currentTenant();
        Specification<CrmTodoItem> spec = specForTenant(t.getId())
                .and(specDateRange(f))
                .and(specPriority(f))
                .and(specStatus(f))
                .and(specSubject(f))
                .and(specLabel(f != null ? f.getLabelId() : null))
                .and(specEmployees(f != null ? f.getEmployeeIds() : null));

        Pageable sorted = ensureSort(pageable, f);
        return todoRepo.findAll(spec, sorted).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CrmTodoSubjectCount> subjectCounts() {
        Tenant t = currentTenant();
        return todoRepo.countBySubject(t.getId()).stream()
                .map(r -> new CrmTodoSubjectCount((TaskSubject) r[0], ((Number) r[1]).longValue()))
                .collect(Collectors.toList());
    }

    /* ------------- labels -------------- */

    public CrmTodoLabelResponse createLabel(CrmTodoLabelRequest req) {
        Tenant t = currentTenant();
        CrmTodoLabel l = CrmTodoLabel.builder()
                .tenant(t)
                .name(req.getName().trim())
                .colorHex(normalizeHex(req.getColorHex()))
                .starred(req.isStarred())
                .build();
        return toLabelResponse(labelRepo.save(l));
    }

    public CrmTodoLabelResponse updateLabel(Long id, CrmTodoLabelRequest req) {
        Tenant t = currentTenant();
        CrmTodoLabel l = labelRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
        l.setName(req.getName().trim());
        l.setColorHex(normalizeHex(req.getColorHex()));
        l.setStarred(req.isStarred());
        return toLabelResponse(labelRepo.save(l));
    }

    public void deleteLabel(Long id) {
        Tenant t = currentTenant();
        CrmTodoLabel l = labelRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
        labelRepo.delete(l);
    }

    @Transactional(readOnly = true)
    public List<CrmTodoLabelResponse> getLabels() {
        Tenant t = currentTenant();
        return labelRepo.findByTenantIdOrderByNameAsc(t.getId())
                .stream().map(this::toLabelResponse).collect(Collectors.toList());
    }

    /* -------- helper mapping -------- */

    private void apply(CrmTodoRequest req, CrmTodoItem e) {
        if (req.getSubject() != null) e.setSubject(req.getSubject());
        e.setDescription(req.getDescription());
        e.setDueDate(req.getDueDate());
        e.setFromTime(req.getFromTime());
        e.setToTime(req.getToTime());
        if (req.getStatus() != null) e.setStatus(req.getStatus());
        if (req.getPriority() != null) e.setPriority(req.getPriority());
        e.setCustomerContactName(req.getCustomerContactName());

        if (req.getAssignedToEmployeeId() != null) {
            Employee assignee = employeeRepo.findById(req.getAssignedToEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + req.getAssignedToEmployeeId()));
            e.setAssignedTo(assignee);
        } else {
            e.setAssignedTo(null);
        }

        if (req.getEmployeeIds() != null) {
            e.getEmployees().clear();
            e.getEmployees().addAll(new LinkedHashSet<>(employeeRepo.findAllById(req.getEmployeeIds())));
        }

        if (req.getContactIds() != null) {
            e.getContacts().clear();
            e.getContacts().addAll(new LinkedHashSet<>(contactRepo.findAllById(req.getContactIds())));
        }

        if (req.getLabelIds() != null) {
            Tenant t = e.getTenant();
            List<CrmTodoLabel> labels = labelRepo.findAllByIdInAndTenantId(req.getLabelIds(), t.getId());
            e.getLabels().clear();
            e.getLabels().addAll(new LinkedHashSet<>(labels));
        }
    }

    private CrmTodoResponse toResponse(CrmTodoItem e) {
        DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneOffset.UTC);
        return CrmTodoResponse.builder()
                .id(e.getId())
                .subject(e.getSubject())
                .description(e.getDescription())
                .dueDate(e.getDueDate())
                .fromTime(e.getFromTime())
                .toTime(e.getToTime())
                .status(e.getStatus())
                .priority(e.getPriority())
                .customerContactName(e.getCustomerContactName())
                .assignedToId(e.getAssignedTo() != null ? e.getAssignedTo().getId() : null)
                .assignedToName(e.getAssignedTo() != null
                        ? ((e.getAssignedTo().getFirstName() == null ? "" : e.getAssignedTo().getFirstName()) + " " +
                           (e.getAssignedTo().getLastName() == null ? "" : e.getAssignedTo().getLastName())).trim())
                .employees(e.getEmployees().stream()
                        .map(emp -> new SimpleIdNameDto(emp.getId(),
                                ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " +
                                        (emp.getLastName() == null ? "" : emp.getLastName())).trim()))
                        .collect(Collectors.toList()))
                .contacts(e.getContacts().stream()
                        .map(c -> new SimpleIdNameDto(c.getId(), c.getFullName()))
                        .collect(Collectors.toList()))
                .labels(e.getLabels().stream()
                        .map(l -> new SimpleIdNameDto(l.getId(), l.getName()))
                        .collect(Collectors.toList()))
                .createdAt(e.getCreatedAt() != null ? ISO.format(e.getCreatedAt()) : null)
                .updatedAt(e.getUpdatedAt() != null ? ISO.format(e.getUpdatedAt()) : null)
                .build();
    }

    private CrmTodoLabelResponse toLabelResponse(CrmTodoLabel l) {
        return CrmTodoLabelResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .colorHex(l.getColorHex())
                .starred(l.isStarred())
                .build();
    }

    private String normalizeHex(String hex) {
        if (hex == null) return null;
        String h = hex.trim();
        if (h.isEmpty()) return null;
        return h.startsWith("#") ? h : "#" + h;
    }

    /* -------- specifications + sort -------- */

    private Specification<CrmTodoItem> specForTenant(Long tenantId) {
        return (root, q, cb) -> cb.equal(root.get("tenant").get("id"), tenantId);
    }

    private Specification<CrmTodoItem> specDateRange(CrmTodoFilterRequest f) {
        if (f == null || f.getDateMode() == null) return null;

        LocalDate from;
        LocalDate to;

        switch (f.getDateMode()) {
            case TODAY -> { from = LocalDate.now(); to = from; }
            case NEXT_DAY -> { from = LocalDate.now().plusDays(1); to = from; }
            case NEXT_WEEK -> { from = LocalDate.now(); to = LocalDate.now().plusWeeks(1); }
            case LAST_2_WEEKS -> { from = LocalDate.now().minusWeeks(2); to = LocalDate.now(); }
            case CUSTOM -> { from = f.getFromDate(); to = f.getToDate(); }
            default -> { return null; }
        }

        if (from == null && to == null) return null;

        return (root, q, cb) -> {
            if (from != null && to != null) return cb.between(root.get("dueDate"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("dueDate"), from);
            return cb.lessThanOrEqualTo(root.get("dueDate"), to);
        };
    }

    private Specification<CrmTodoItem> specPriority(CrmTodoFilterRequest f) {
        if (f == null || f.getPriority() == null) return null;
        return (root, q, cb) -> cb.equal(root.get("priority"), f.getPriority());
    }

    private Specification<CrmTodoItem> specStatus(CrmTodoFilterRequest f) {
        if (f == null || f.getStatus() == null) return null;
        return (root, q, cb) -> cb.equal(root.get("status"), f.getStatus());
    }

    private Specification<CrmTodoItem> specSubject(CrmTodoFilterRequest f) {
        if (f == null || f.getSubject() == null) return null;
        return (root, q, cb) -> cb.equal(root.get("subject"), f.getSubject());
    }

    private Specification<CrmTodoItem> specLabel(Long labelId) {
        if (labelId == null) return null;
        return (root, q, cb) -> {
            q.distinct(true);
            return cb.equal(root.join("labels").get("id"), labelId);
        };
    }

    private Specification<CrmTodoItem> specEmployees(Set<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) return null;
        return (root, q, cb) -> {
            q.distinct(true);
            return root.join("employees").get("id").in(employeeIds);
        };
    }

    private Pageable ensureSort(Pageable pageable, CrmTodoFilterRequest f) {
        if (pageable.getSort().isSorted()) return pageable;
        CrmTodoFilterRequest.SortBy by = f != null ? f.getSortBy() : CrmTodoFilterRequest.SortBy.CREATED_DATE;
        CrmTodoFilterRequest.Direction dir = f != null ? f.getDirection() : CrmTodoFilterRequest.Direction.DESC;

        String column = switch (by) {
            case DUE_DATE -> "dueDate";
            case UPDATED_DATE -> "updatedAt";
            default -> "createdAt";
        };
        Sort sort = (dir == CrmTodoFilterRequest.Direction.ASC) ? Sort.by(column).ascending() : Sort.by(column).descending();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
