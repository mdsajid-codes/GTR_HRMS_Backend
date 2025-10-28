package com.example.multi_tanent.production.services;

import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.production.dto.ProInventoryTypeDto;
import com.example.multi_tanent.production.dto.ProInventoryTypeRequest;
import com.example.multi_tanent.production.entity.ProInventoryType;
import com.example.multi_tanent.production.repository.ProInventoryTypeRepository;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional("tenantTx")
public class ProInventoryTypeService {

    private final ProInventoryTypeRepository inventoryTypeRepository;
    private final TenantRepository tenantRepository;

    public ProInventoryTypeService(ProInventoryTypeRepository inventoryTypeRepository, TenantRepository tenantRepository) {
        this.inventoryTypeRepository = inventoryTypeRepository;
        this.tenantRepository = tenantRepository;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found. Cannot perform operations."));
    }

    public ProInventoryTypeDto createInventoryType(ProInventoryTypeRequest request) {
        Tenant tenant = getCurrentTenant();
        ProInventoryType inventoryType = ProInventoryType.builder()
                .tenant(tenant)
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        ProInventoryType saved = inventoryTypeRepository.save(inventoryType);
        return toDto(saved);
    }

    public List<ProInventoryTypeDto> getAllInventoryTypes() {
        return inventoryTypeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProInventoryTypeDto getInventoryTypeById(Long id) {
        return inventoryTypeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Inventory Type not found with id: " + id));
    }

    public ProInventoryTypeDto updateInventoryType(Long id, ProInventoryTypeRequest request) {
        ProInventoryType inventoryType = inventoryTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory Type not found with id: " + id));

        inventoryType.setName(request.getName());
        inventoryType.setDescription(request.getDescription());

        ProInventoryType updated = inventoryTypeRepository.save(inventoryType);
        return toDto(updated);
    }

    public void deleteInventoryType(Long id) {
        if (!inventoryTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventory Type not found with id: " + id);
        }
        inventoryTypeRepository.deleteById(id);
    }

    private ProInventoryTypeDto toDto(ProInventoryType entity) {
        return new ProInventoryTypeDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
