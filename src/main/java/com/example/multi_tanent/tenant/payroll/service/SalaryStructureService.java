package com.example.multi_tanent.tenant.payroll.service;

import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.payroll.dto.SalaryStructureRequest;
import com.example.multi_tanent.tenant.payroll.entity.SalaryComponent;
import com.example.multi_tanent.tenant.payroll.entity.SalaryStructure;
import com.example.multi_tanent.tenant.payroll.entity.SalaryStructureComponent;
import com.example.multi_tanent.tenant.payroll.repository.SalaryComponentRepository;
import com.example.multi_tanent.tenant.payroll.repository.SalaryStructureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(transactionManager = "tenantTx")
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final EmployeeRepository employeeRepository;
    private final SalaryComponentRepository salaryComponentRepository;

    public SalaryStructureService(SalaryStructureRepository salaryStructureRepository, EmployeeRepository employeeRepository, SalaryComponentRepository salaryComponentRepository) {
        this.salaryStructureRepository = salaryStructureRepository;
        this.employeeRepository = employeeRepository;
        this.salaryComponentRepository = salaryComponentRepository;
    }

    public List<SalaryStructure> getAllSalaryStructures() {
        List<SalaryStructure> structures = salaryStructureRepository.findAll();
        structures.forEach(this::initializeStructureDetails);
        return structures;
    }

    public Optional<SalaryStructure> getSalaryStructureByEmployeeCode(String employeeCode) {
        Optional<SalaryStructure> structureOpt = salaryStructureRepository.findByEmployeeEmployeeCode(employeeCode);
        structureOpt.ifPresent(this::initializeStructureDetails);
        return structureOpt;
    }

    public SalaryStructure createSalaryStructure(SalaryStructureRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        salaryStructureRepository.findByEmployeeId(employee.getId()).ifPresent(s -> {
            throw new IllegalStateException("A salary structure already exists for employee: " + request.getEmployeeCode());
        });

        SalaryStructure structure = new SalaryStructure();
        structure.setEmployee(employee);
        mapRequestToEntity(request, structure);

        SalaryStructure savedStructure = salaryStructureRepository.save(structure);
        initializeStructureDetails(savedStructure);
        return savedStructure;
    }

    public SalaryStructure updateSalaryStructure(Long id, SalaryStructureRequest request) {
        SalaryStructure structure = salaryStructureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalaryStructure not found with id: " + id));

        if (!structure.getEmployee().getEmployeeCode().equals(request.getEmployeeCode())) {
            throw new IllegalArgumentException("Cannot change the employee associated with a salary structure.");
        }

        mapRequestToEntity(request, structure);
        SalaryStructure savedStructure = salaryStructureRepository.save(structure);
        initializeStructureDetails(savedStructure);
        return savedStructure;
    }

    public void deleteSalaryStructure(Long id) {
        salaryStructureRepository.deleteById(id);
    }

    private void mapRequestToEntity(SalaryStructureRequest request, SalaryStructure structure) {
        structure.setStructureName(request.getStructureName());
        structure.setEffectiveDate(request.getEffectiveDate());

        if (structure.getComponents() == null) {
            structure.setComponents(new ArrayList<>());
        }
        structure.getComponents().clear();

        List<SalaryStructureComponent> newComponents = request.getComponents().stream()
                .map(compRequest -> {
                    SalaryComponent salaryComponent = salaryComponentRepository.findByCode(compRequest.getComponentCode())
                            .orElseThrow(() -> new RuntimeException("SalaryComponent not found with code: " + compRequest.getComponentCode()));

                    SalaryStructureComponent ssc = new SalaryStructureComponent();
                    ssc.setSalaryStructure(structure);
                    ssc.setSalaryComponent(salaryComponent);
                    ssc.setValue(compRequest.getValue());
                    ssc.setFormula(compRequest.getFormula());
                    return ssc;
                }).collect(Collectors.toList());

        structure.getComponents().addAll(newComponents);
    }

    private void initializeStructureDetails(SalaryStructure structure) {
        // Accessing these getters will trigger the lazy loading while the session is active.
        if (structure.getEmployee() != null) {
            structure.getEmployee().getEmployeeCode(); // Initialize Employee proxy
        }
        if (structure.getComponents() != null) {
            structure.getComponents().size(); // Initialize components collection
            // Also initialize the SalaryComponent inside each SalaryStructureComponent
            for (SalaryStructureComponent ssc : structure.getComponents()) {
                if (ssc.getSalaryComponent() != null) {
                    ssc.getSalaryComponent().getCode();
                }
            }
        }
    }
}