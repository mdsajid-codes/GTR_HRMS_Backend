package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.payroll.dto.SalaryComponentRequest;
import com.example.multi_tanent.tenant.payroll.entity.SalaryComponent;
import com.example.multi_tanent.tenant.payroll.repository.SalaryComponentRepository;
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
        salaryComponentRepository.findByCode(request.getCode()).ifPresent(c -> {
            throw new IllegalStateException("Salary component with code '" + request.getCode() + "' already exists.");
        });
        SalaryComponent component = new SalaryComponent();
        mapRequestToEntity(request, component);
        return salaryComponentRepository.save(component);
    }

    public SalaryComponent updateSalaryComponent(Long id, SalaryComponentRequest request) {
        salaryComponentRepository.findByCode(request.getCode())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new IllegalStateException("Another salary component with code '" + request.getCode() + "' already exists.");
                });

        SalaryComponent component = salaryComponentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryComponent not found with id: " + id));
        mapRequestToEntity(request, component);
        return salaryComponentRepository.save(component);
    }

    public void deleteSalaryComponent(Long id) {
        salaryComponentRepository.deleteById(id);
    }

    private void mapRequestToEntity(SalaryComponentRequest req, SalaryComponent entity) {
        entity.setName(req.getName());
        entity.setCode(req.getCode());
        entity.setType(req.getType());
        entity.setCalculationType(req.getCalculationType());
        entity.setFormula(req.getFormula());
        entity.setTaxable(req.isTaxable());
        entity.setPartOfGrossSalary(req.isPartOfGrossSalary());
        entity.setDisplayOrder(req.getDisplayOrder());
    }
}