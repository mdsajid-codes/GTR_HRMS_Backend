package com.example.multi_tanent.sales.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.sales.dto.SalesTermAndConditionRequest;
import com.example.multi_tanent.sales.dto.SalesTermAndConditionResponse;
import com.example.multi_tanent.sales.entity.SalesTermAndCondition;
import com.example.multi_tanent.sales.repository.SalesTermAndConditionRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.spersusers.repository.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesTermAndConditionService {

    private final SalesTermAndConditionRepository repository;
    private final TenantRepository tenantRepository;

    private Tenant getCurrentTenant() {
        String tenantId = TenantContext.getTenantId();
        return tenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant not found: " + tenantId));
    }

    public SalesTermAndConditionResponse create(SalesTermAndConditionRequest request) {
        Tenant tenant = getCurrentTenant();
        if (repository.existsByTenantIdAndNameIgnoreCase(tenant.getId(), request.getName())) {
            throw new IllegalArgumentException("Terms & Condition with name '" + request.getName() + "' already exists.");
        }

        SalesTermAndCondition entity = new SalesTermAndCondition();
        mapRequestToEntity(request, entity, tenant);

        SalesTermAndCondition savedEntity = repository.save(entity);

        if (savedEntity.isDefault()) {
            repository.clearDefaultFlagForTenant(tenant.getId(), savedEntity.getId());
        }

        return SalesTermAndConditionResponse.fromEntity(savedEntity);
    }

    public SalesTermAndConditionResponse update(Long id, SalesTermAndConditionRequest request) {
        Tenant tenant = getCurrentTenant();
        SalesTermAndCondition entity = repository.findByTenantIdAndId(tenant.getId(), id)
                .orElseThrow(() -> new EntityNotFoundException("Terms & Condition not found: " + id));

        mapRequestToEntity(request, entity, tenant);
        SalesTermAndCondition savedEntity = repository.save(entity);

        if (savedEntity.isDefault()) {
            repository.clearDefaultFlagForTenant(tenant.getId(), savedEntity.getId());
        }

        return SalesTermAndConditionResponse.fromEntity(savedEntity);
    }

    @Transactional(readOnly = true)
    public List<SalesTermAndConditionResponse> getAll() {
        Long tenantId = getCurrentTenant().getId();
        return repository.findByTenantIdOrderByNameAsc(tenantId).stream()
                .map(SalesTermAndConditionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalesTermAndConditionResponse getById(Long id) {
        Long tenantId = getCurrentTenant().getId();
        return repository.findByTenantIdAndId(tenantId, id)
                .map(SalesTermAndConditionResponse::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("Terms & Condition not found: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Terms & Condition not found: " + id);
        }
        repository.deleteById(id);
    }

    private void mapRequestToEntity(SalesTermAndConditionRequest request, SalesTermAndCondition entity, Tenant tenant) {
        entity.setTenant(tenant);
        entity.setName(request.getName());
        entity.setContent(request.getContent());
        entity.setDefault(request.isDefault());
    }
}