// com/example/multi_tanent/tenant/controller/EmployeeController.java
package com.example.multi_tanent.tenant.controller;

import com.example.multi_tanent.tenant.entity.Employee;
import com.example.multi_tanent.tenant.entity.User;
import com.example.multi_tanent.tenant.entity.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.entity.enums.Gender;
import com.example.multi_tanent.tenant.entity.enums.MartialStatus;
import com.example.multi_tanent.tenant.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.repository.UserRepository;
import com.example.multi_tanent.tenant.tenantDto.EmployeeRequest;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
  @Autowired
  private EmployeeRepository empRepo;
  @Autowired
  private UserRepository userRepo;

  public Optional<User> user(String email){
    return userRepo.findByEmail(email);
  }

  @PostMapping("/register")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<String> registerUser(@RequestBody EmployeeRequest request){
    Optional<User> existingUser = user(request.getEmail());
    if(!existingUser.isPresent()){
      return ResponseEntity.badRequest().body("User not found");
    }

    Employee e = new Employee();
    e.setEmployeeCode(request.getEmployeeCode());
    e.setFirstName(request.getFirstName());
    e.setMiddleName(request.getMiddleName());
    e.setLastName(request.getLastName());
    e.setEmailWork(request.getEmailWork());
    e.setEmailPersonal(request.getEmailPersonal());
    e.setPhonePrimary(request.getPhonePrimary());
    e.setPhoneSecondary(request.getPhoneSecondary());
    e.setDob(request.getDob());
    e.setGender(request.getGender());
    e.setMartialStatus(request.getMartialStatus());
    e.setCurrentAddress(request.getCurrentAddress());
    e.setPermanentAddress(request.getPermanentAddress());
    e.setNationalIdType(request.getNationalIdType());
    e.setNationalIdNumber(request.getNationalIdNumber());
    e.setStatus(request.getStatus());

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    e.setCreatedBy(username);
    e.setUpdatedBy(username);
    e.setCreatedAt(LocalDateTime.now());
    e.setUpdatedAt(LocalDateTime.now());
    e.setUser(existingUser.get());
    
    empRepo.save(e);
    return ResponseEntity.ok("Employee registered successfully");
  }

  @PostMapping("/bulkEmployees")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<String> addBulkEmployees(@RequestParam("file") MultipartFile file){
    try(InputStream is = file.getInputStream()){
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);

      for(int i=1; i<= sheet.getLastRowNum(); i++){
        Row row = sheet.getRow(i);
        if(row == null) continue;

        Employee employee = new Employee();
        String email = row.getCell(0).getStringCellValue();
        Optional<User> user = userRepo.findByEmail(email);
        if(user.isEmpty()){
            // Log a warning or handle the case where the user does not exist
            System.err.println("User with email " + email + " not found. Skipping employee registration for this row.");
            continue; 
        }
        employee.setUser(user.get());
        employee.setEmployeeCode(row.getCell(1).getStringCellValue());
        employee.setFirstName(row.getCell(2).getStringCellValue());
        employee.setMiddleName(row.getCell(3).getStringCellValue());
        employee.setLastName(row.getCell(4).getStringCellValue());
        employee.setEmailWork(row.getCell(5).getStringCellValue());
        employee.setEmailPersonal(row.getCell(6).getStringCellValue());
        employee.setPhonePrimary(row.getCell(7).getStringCellValue());
        employee.setPhoneSecondary(row.getCell(8).getStringCellValue());
        employee.setDob(row.getCell(9).getLocalDateTimeCellValue().toLocalDate());
        employee.setGender(Gender.valueOf(row.getCell(10).getStringCellValue().toUpperCase()));
        employee.setMartialStatus(MartialStatus.valueOf(row.getCell(11).getStringCellValue().toUpperCase()));
        employee.setCurrentAddress(row.getCell(12).getStringCellValue());
        employee.setPermanentAddress(row.getCell(13).getStringCellValue());
        employee.setNationalIdType(row.getCell(14).getStringCellValue());
        employee.setNationalIdNumber(row.getCell(15).getStringCellValue());
        employee.setStatus(EmployeeStatus.valueOf(row.getCell(16).getStringCellValue().toUpperCase()));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        employee.setCreatedBy(username);
        employee.setUpdatedBy(username);
        employee.setCreatedAt(LocalDateTime.now());
        employee.setUpdatedAt(LocalDateTime.now());
        empRepo.save(employee);
      
      }
          return ResponseEntity.ok("Bulk employees added successfully");
      } catch (Exception e) {
          return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
      }
  }

  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public List<Employee> all() {
      return empRepo.findAll();
  }

  @GetMapping("/{employeeCode}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<Employee> fetchByEmployeeCode(@PathVariable String employeeCode){
    return empRepo.findByEmployeeCode(employeeCode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-gender/{gender}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<?> fetchByGender(@PathVariable String gender){
    try {
        Gender genderEnum = Gender.valueOf(gender.toUpperCase());
        List<Employee> employees = empRepo.findByGender(genderEnum);
        return ResponseEntity.ok(employees);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid gender value: " + gender);
    }
  }

  @GetMapping("/by-martial-status/{martialStatus}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<?> fetchByMartialStatus(@PathVariable String martialStatus){
    try {
        MartialStatus statusEnum = MartialStatus.valueOf(martialStatus.toUpperCase());
        List<Employee> employees = empRepo.findByMartialStatus(statusEnum);
        return ResponseEntity.ok(employees);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid martial status value: " + martialStatus);
    }
  }

  @GetMapping("/by-status/{status}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<?> fetchByStatus(@PathVariable String status){
    try {
        EmployeeStatus statusEnum = EmployeeStatus.valueOf(status.toUpperCase());
        List<Employee> employees = empRepo.findByStatus(statusEnum);
        return ResponseEntity.ok(employees);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid status value: " + status);
    }
  }
  
  @PutMapping("/{employeeCode}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR','MANAGER')")
  public ResponseEntity<Employee> updateEmployee(@PathVariable String employeeCode, @RequestBody EmployeeRequest empRequest){
    return empRepo.findByEmployeeCode(employeeCode)
        .map(employee -> {
            updateEmployeeFromRequest(employee, empRequest);
            Employee updatedEmployee = empRepo.save(employee);
            return ResponseEntity.ok(updatedEmployee);
        })
        .orElse(ResponseEntity.notFound().build());
  }

  private void updateEmployeeFromRequest(Employee employee, EmployeeRequest request) {
    // The 'email' field from EmployeeRequest is ignored for identification.
    // If the user association needs to be changed, that should be a separate, explicit action.
    // The employeeCode is the resource identifier from the path and should not be changed here.
    // If changing an employee code is a required feature, it should be a more explicit and secured operation.
    employee.setEmployeeCode(request.getEmployeeCode());
    employee.setFirstName(request.getFirstName());
    employee.setMiddleName(request.getMiddleName());
    employee.setLastName(request.getLastName());
    employee.setEmailWork(request.getEmailWork());
    employee.setEmailPersonal(request.getEmailPersonal());
    employee.setPhonePrimary(request.getPhonePrimary());
    employee.setPhoneSecondary(request.getPhoneSecondary());
    employee.setDob(request.getDob());
    employee.setGender(request.getGender());
    employee.setMartialStatus(request.getMartialStatus());
    employee.setCurrentAddress(request.getCurrentAddress());
    employee.setPermanentAddress(request.getPermanentAddress());
    employee.setNationalIdType(request.getNationalIdType());
    employee.setNationalIdNumber(request.getNationalIdNumber());
    employee.setStatus(request.getStatus());

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    employee.setUpdatedBy(username);
    employee.setUpdatedAt(LocalDateTime.now());
  }

}
