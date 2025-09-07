package com.example.multi_tanent.tenant.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.multi_tanent.tenant.entity.Department;
import com.example.multi_tanent.tenant.repository.DepartmentRepository;
import com.example.multi_tanent.tenant.tenantDto.DepartmentRequest;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<Department> createDepartment(@RequestBody DepartmentRequest request){
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        department.setDescription(request.getDescription());
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        Department savedDepartment = departmentRepository.save(department);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
            .buildAndExpand(savedDepartment.getId()).toUri();

        return ResponseEntity.created(location).body(savedDepartment);
    }

    @GetMapping
    public ResponseEntity<List<Department>> getDepartment(){
        return ResponseEntity.ok(departmentRepository.findAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    public ResponseEntity<String> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest departmentRequest){
        Optional<Department> existDeparment = departmentRepository.findById(id);

        if(existDeparment.isPresent()){
            Department department = existDeparment.get();
            department.setName(departmentRequest.getName());
            department.setCode(departmentRequest.getCode());
            department.setDescription(departmentRequest.getDescription());
            department.setUpdatedAt(LocalDateTime.now());
            departmentRepository.save(department);
            return ResponseEntity.ok("Department Updated Successfully");
        }else{
            return ResponseEntity.badRequest().body("Failde to update!");
        }

    }

    @DeleteMapping("/{departmentName}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
    @Transactional("tenantTx")
    public ResponseEntity<String> deleteDepartment(@PathVariable String departmentName){
        long deletedCount = departmentRepository.deleteByName(departmentName);
        if (deletedCount > 0) {
            return ResponseEntity.ok("Department(s) with name '" + departmentName + "' deleted successfully.");
        }
        return ResponseEntity.notFound().build();
    }
}
