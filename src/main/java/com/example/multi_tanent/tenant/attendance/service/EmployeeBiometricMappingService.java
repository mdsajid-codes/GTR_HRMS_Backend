package com.example.multi_tanent.tenant.attendance.service;

import com.example.multi_tanent.tenant.attendance.dto.EmployeeBiometricMappingRequest;
import com.example.multi_tanent.tenant.attendance.entity.BiometricDevice;
import com.example.multi_tanent.tenant.attendance.entity.EmployeeBiometricMapping;
import com.example.multi_tanent.tenant.attendance.repository.BiometricDeviceRepository;
import com.example.multi_tanent.tenant.attendance.repository.EmployeeBiometricMappingRepository;
import com.example.multi_tanent.tenant.employee.entity.Employee;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(transactionManager = "tenantTx")
public class EmployeeBiometricMappingService {

    private final EmployeeBiometricMappingRepository mappingRepository;
    private final EmployeeRepository employeeRepository;
    private final BiometricDeviceRepository deviceRepository;

    public EmployeeBiometricMappingService(EmployeeBiometricMappingRepository mappingRepository,
                                           EmployeeRepository employeeRepository,
                                           BiometricDeviceRepository deviceRepository) {
        this.mappingRepository = mappingRepository;
        this.employeeRepository = employeeRepository;
        this.deviceRepository = deviceRepository;
    }

    public EmployeeBiometricMapping createMapping(EmployeeBiometricMappingRequest request) {
        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        BiometricDevice device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Biometric device not found with id: " + request.getDeviceId()));

        // Check for duplicate biometric identifier on the same device
        mappingRepository.findByBiometricIdentifierAndDeviceId(request.getBiometricIdentifier(), request.getDeviceId())
                .ifPresent(m -> {
                    throw new RuntimeException("Biometric identifier '" + request.getBiometricIdentifier() + "' is already registered on this device.");
                });

        EmployeeBiometricMapping mapping = new EmployeeBiometricMapping();
        mapping.setEmployee(employee);
        mapping.setDevice(device);
        mapping.setBiometricIdentifier(request.getBiometricIdentifier());
        mapping.setIdentifierType(request.getIdentifierType());
        mapping.setActive(request.getActive());
        mapping.setExternalReference(request.getExternalReference());
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());

        return mappingRepository.save(mapping);
    }

    @Transactional(readOnly = true)
    public List<EmployeeBiometricMapping> getAllMappings() {
        return mappingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<EmployeeBiometricMapping> getMappingById(Long id) {
        return mappingRepository.findById(id);
    }

    public EmployeeBiometricMapping updateMapping(Long id, EmployeeBiometricMappingRequest request) {
        EmployeeBiometricMapping mapping = mappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found with id: " + id));

        Employee employee = employeeRepository.findByEmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new RuntimeException("Employee not found with code: " + request.getEmployeeCode()));

        BiometricDevice device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Biometric device not found with id: " + request.getDeviceId()));

        mapping.setEmployee(employee);
        mapping.setDevice(device);
        mapping.setBiometricIdentifier(request.getBiometricIdentifier());
        mapping.setIdentifierType(request.getIdentifierType());
        mapping.setActive(request.getActive());
        mapping.setExternalReference(request.getExternalReference());
        mapping.setUpdatedAt(LocalDateTime.now());

        return mappingRepository.save(mapping);
    }

    public void deleteMapping(Long id) {
        if (!mappingRepository.existsById(id)) {
            throw new RuntimeException("Mapping not found with id: " + id);
        }
        mappingRepository.deleteById(id);
    }
}