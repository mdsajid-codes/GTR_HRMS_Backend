package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.payroll.dto.SalaryComponentRequest;
import com.example.multi_tanent.tenant.payroll.entity.SalaryComponent;
import com.example.multi_tanent.tenant.payroll.repository.SalaryComponentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class SalaryComponentService {

    private final SalaryComponentRepository salaryComponentRepository;

    public SalaryComponentService(SalaryComponentRepository salaryComponentRepository) {
        this.salaryComponentRepository = salaryComponentRepository;
    }

    public List<SalaryComponent> getAllSalaryComponents() {
        return salaryComponentRepository.findAll();
    }

    public Optional<SalaryComponent> getSalaryComponentById(Long id) {
        return salaryComponentRepository.findById(id);
    }

    public SalaryComponent createSalaryComponent(SalaryComponentRequest request) {
        salaryComponentRepository.findByCode(request.getCode()).ifPresent(sc -> {
            throw new IllegalStateException("Salary component with code '" + request.getCode() + "' already exists.");
        });

        SalaryComponent component = new SalaryComponent();
        mapRequestToEntity(request, component);
        return salaryComponentRepository.save(component);
    }

    public SalaryComponent updateSalaryComponent(Long id, SalaryComponentRequest request) {
        SalaryComponent component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SalaryComponent not found with id: " + id));

        // Check if the code is being changed and if the new code already exists for another component
        if (!component.getCode().equalsIgnoreCase(request.getCode())) {
            salaryComponentRepository.findByCode(request.getCode()).ifPresent(sc -> {
                if (!sc.getId().equals(id)) {
                    throw new IllegalStateException("Salary component with code '" + request.getCode() + "' already exists.");
                }
            });
        }

        mapRequestToEntity(request, component);
        return salaryComponentRepository.save(component);
    }

    public void deleteSalaryComponent(Long id) {
        if (!salaryComponentRepository.existsById(id)) {
            throw new EntityNotFoundException("SalaryComponent not found with id: " + id);
        }
        // TODO: Add a check here to prevent deletion if the component is used in any SalaryStructure
        salaryComponentRepository.deleteById(id);
    }

    private void mapRequestToEntity(SalaryComponentRequest request, SalaryComponent entity) {
        entity.setCode(request.getCode().toUpperCase()); // Store codes in uppercase for consistency
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setCalculationType(request.getCalculationType());
        entity.setTaxable(request.getIsTaxable());
        entity.setPartOfGrossSalary(request.getIsPartOfGrossSalary());
    }
}