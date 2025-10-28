package com.example.multi_tanent.production.services;

import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.production.dto.EmployeeSlimDto;
import com.example.multi_tanent.production.dto.ProWorkStationDto;
import com.example.multi_tanent.production.dto.ProWorkStationRequest;
import com.example.multi_tanent.production.entity.ProWorkGroup;
import com.example.multi_tanent.production.entity.ProWorkStation;
import com.example.multi_tanent.production.repository.ProWorkGroupRepository;
import com.example.multi_tanent.production.repository.ProWorkStationRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Tenant;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional("tenantTx")
public class ProWorkStationService {

    private final ProWorkStationRepository workStationRepository;
    private final ProWorkGroupRepository workGroupRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantRepository tenantRepository;

    public ProWorkStationService(ProWorkStationRepository workStationRepository, ProWorkGroupRepository workGroupRepository, EmployeeRepository employeeRepository, TenantRepository tenantRepository) {
        this.workStationRepository = workStationRepository;
        this.workGroupRepository = workGroupRepository;
        this.employeeRepository = employeeRepository;
        this.tenantRepository = tenantRepository;
    }

    private Tenant getCurrentTenant() {
        return tenantRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Tenant context not found."));
    }

    public ProWorkStationDto createWorkStation(ProWorkStationRequest request) {
        Tenant tenant = getCurrentTenant();
        ProWorkGroup workGroup = workGroupRepository.findById(request.getWorkGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Work Group not found with id: " + request.getWorkGroupId()));

        List<Employee> employees = fetchEmployees(request.getEmployeeIds());

        ProWorkStation workStation = ProWorkStation.builder()
                .tenant(tenant)
                .workGroup(workGroup)
                .workstationName(request.getWorkstationName())
                .employees(employees)
                .build();

        ProWorkStation saved = workStationRepository.save(workStation);
        return toDto(saved);
    }

    public List<ProWorkStationDto> getAllWorkStations() {
        return workStationRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProWorkStationDto getWorkStationById(Long id) {
        return workStationRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Workstation not found with id: " + id));
    }

    public ProWorkStationDto updateWorkStation(Long id, ProWorkStationRequest request) {
        ProWorkStation workStation = workStationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workstation not found with id: " + id));

        ProWorkGroup workGroup = workGroupRepository.findById(request.getWorkGroupId())
                .orElseThrow(() -> new EntityNotFoundException("Work Group not found with id: " + request.getWorkGroupId()));

        List<Employee> employees = fetchEmployees(request.getEmployeeIds());

        workStation.setWorkGroup(workGroup);
        workStation.setWorkstationName(request.getWorkstationName());
        workStation.setEmployees(employees);

        ProWorkStation updated = workStationRepository.save(workStation);
        return toDto(updated);
    }

    public void deleteWorkStation(Long id) {
        if (!workStationRepository.existsById(id)) {
            throw new EntityNotFoundException("Workstation not found with id: " + id);
        }
        workStationRepository.deleteById(id);
    }

    private List<Employee> fetchEmployees(List<Long> employeeIds) {
        return (employeeIds == null || employeeIds.isEmpty()) ? Collections.emptyList() : employeeRepository.findAllById(employeeIds);
    }

    private ProWorkStationDto toDto(ProWorkStation entity) {
        return ProWorkStationDto.builder()
                .id(entity.getId())
                .workstationNumber(entity.getWorkstationNumber())
                .workstationName(entity.getWorkstationName())
                .workGroupName(entity.getWorkGroup().getName())
                .employees(entity.getEmployees().stream()
                        .map(emp -> new EmployeeSlimDto(emp.getId(), emp.getFirstName() + " " + emp.getLastName()))
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}