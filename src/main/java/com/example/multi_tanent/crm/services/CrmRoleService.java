package com.example.multi_tanent.crm.services;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.crm.dto.CrmPermissionDto;
import com.example.multi_tanent.crm.dto.CrmRoleRequest;
import com.example.multi_tanent.crm.dto.CrmRoleResponse;
import com.example.multi_tanent.crm.entity.CrmPermission;
import com.example.multi_tanent.crm.entity.CrmRole;
import com.example.multi_tanent.crm.entity.CrmRolePermission;
import com.example.multi_tanent.crm.repository.CrmPermissionRepository;
import com.example.multi_tanent.crm.repository.CrmRolePermissionRepository;
import com.example.multi_tanent.crm.repository.CrmRoleRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional("tenantTx")
public class CrmRoleService {

    private final CrmRoleRepository roleRepo;
    private final CrmPermissionRepository permRepo;
    private final CrmRolePermissionRepository rolePermRepo;
    private final TenantRepository tenantRepo;

    private Tenant currentTenant() {
        // Use your actual tenant resolution; fallback to first for demo
        String tenantKey = TenantContext.getTenantId();
        return tenantRepo.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant not found for key: " + tenantKey));
    }

    @Transactional(readOnly = true)
    public List<CrmRoleResponse> getAll() {
        Tenant t = currentTenant();
        List<CrmRole> roles = roleRepo.findByTenantIdOrderByNameAsc(t.getId());
        return roles.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CrmRoleResponse getById(Long id) {
        Tenant t = currentTenant();
        CrmRole role = roleRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new EntityNotFoundException("CrmRole not found: " + id));
        return toResponse(role);
    }

    public CrmRoleResponse create(CrmRoleRequest req) {
        Tenant t = currentTenant();

        if (roleRepo.existsByTenantIdAndNameIgnoreCase(t.getId(), req.getName().trim())) {
            throw new IllegalArgumentException("CrmRole name already exists");
        }

        CrmRole clonedFrom = null;
        if (req.getCloneRoleId() != null) {
            clonedFrom = roleRepo.findByIdAndTenantId(req.getCloneRoleId(), t.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Clone role not found: " + req.getCloneRoleId()));
        }

        CrmRole role = CrmRole.builder()
                .tenant(t)
                .name(req.getName().trim())
                .description(req.getDescription())
                .clonedFrom(clonedFrom)
                .build();
        role = roleRepo.save(role);

        // If clone selected, copy all permissions from source
        if (clonedFrom != null) {
            List<CrmRolePermission> srcPerms = rolePermRepo.findByRole(clonedFrom);
            List<CrmRolePermission> toSave = new ArrayList<>();
            for (CrmRolePermission rp : srcPerms) {
                toSave.add(CrmRolePermission.builder()
                        .role(role)
                        .permission(rp.getPermission())
                        .build());
            }
            rolePermRepo.saveAll(toSave);
        }

        // If explicit permissionIds were provided, replace current set with them
        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            rolePermRepo.deleteByRole(role);
            List<CrmPermission> perms = permRepo.findAllById(req.getPermissionIds());
            List<CrmRolePermission> toSave = perms.stream()
                    .map(p -> CrmRolePermission.builder().role(role).permission(p).build())
                    .collect(Collectors.toList());
            rolePermRepo.saveAll(toSave);
        }

        return toResponse(role);
    }

    public CrmRoleResponse update(Long id, CrmRoleRequest req) {
        Tenant t = currentTenant();
        CrmRole role = roleRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new EntityNotFoundException("CrmRole not found: " + id));

        String newName = req.getName() != null ? req.getName().trim() : role.getName();
        if (!role.getName().equalsIgnoreCase(newName)
                && roleRepo.existsByTenantIdAndNameIgnoreCase(t.getId(), newName)) {
            throw new IllegalArgumentException("CrmRole name already exists");
        }

        role.setName(newName);
        role.setDescription(req.getDescription());
        roleRepo.save(role);

        if (req.getPermissionIds() != null) {
            rolePermRepo.deleteByRole(role);
            List<CrmPermission> perms = permRepo.findAllById(req.getPermissionIds());
            List<CrmRolePermission> toSave = perms.stream()
                    .map(p -> CrmRolePermission.builder().role(role).permission(p).build())
                    .collect(Collectors.toList());
            rolePermRepo.saveAll(toSave);
        }

        return toResponse(role);
    }

    public void delete(Long id) {
        Tenant t = currentTenant();
        CrmRole role = roleRepo.findByIdAndTenantId(id, t.getId())
                .orElseThrow(() -> new EntityNotFoundException("CrmRole not found: " + id));
        rolePermRepo.deleteByRole(role);
        roleRepo.delete(role);
    }

    /* ---------- helpers ---------- */

    private CrmRoleResponse toResponse(CrmRole role) {
        List<CrmRolePermission> links = rolePermRepo.findByRole(role);
        List<CrmPermissionDto> perms = links.stream().map(l -> {
            var p = l.getPermission();
            return CrmPermissionDto.builder()
                    .id(p.getId())
                    .code(p.getCode())
                    .name(p.getName())
                    .description(p.getDescription())
                    .build();
        }).collect(Collectors.toList());

        return CrmRoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .tenantId(role.getTenant().getId())
                .clonedFromId(role.getClonedFrom() != null ? role.getClonedFrom().getId() : null)
                .permissions(perms)
                .build();
    }
}
