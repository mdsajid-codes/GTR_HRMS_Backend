package com.example.multi_tanent.tenant.employee.controller;

import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.User;
import com.example.multi_tanent.spersusers.repository.UserRepository;
import com.example.multi_tanent.tenant.employee.dto.EmployeeRequest;
import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.enums.Gender;
import com.example.multi_tanent.tenant.employee.enums.MartialStatus;
import com.example.multi_tanent.tenant.service.FileStorageService;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin (origins = "*")
@Transactional(transactionManager = "tenantTx")
public class EmployeeController {
  private final EmployeeRepository empRepo;
  private final UserRepository userRepo;
  private final FileStorageService fileStorageService;
  private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);


  public EmployeeController(EmployeeRepository empRepo, UserRepository userRepo, FileStorageService fileStorageService) {
    this.empRepo = empRepo;
    this.userRepo = userRepo;
    this.fileStorageService = fileStorageService;
  }

  @PostMapping("/register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  public ResponseEntity<?> registerEmployee(@RequestBody EmployeeRequest request){
    Optional<User> userOpt = userRepo.findByEmail(request.getEmail());
    if(userOpt.isEmpty()){
      return ResponseEntity.badRequest().body("User with email '" + request.getEmail() + "' not found.");
    }

    if (empRepo.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Employee with code '" + request.getEmployeeCode() + "' already exists.");
    }

    if (empRepo.findByUserId(userOpt.get().getId()).isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("An employee profile already exists for this user.");
    }

    Employee e = new Employee();
    updateEmployeeFromRequest(e, request); // Use helper for creation too
    e.setUser(userOpt.get());

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    e.setCreatedBy(username);
    e.setCreatedAt(LocalDateTime.now());
    
    Employee savedEmployee = empRepo.save(e);

    URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath().path("/api/employees/{employeeCode}")
            .buildAndExpand(savedEmployee.getEmployeeCode()).toUri();

    return ResponseEntity.created(location).body(savedEmployee);
  }

  @PostMapping("/bulkEmployees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  public ResponseEntity<String> addBulkEmployees(@RequestParam("file") MultipartFile file){
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("Cannot process an empty file.");
    }

    List<Employee> employeesToSave = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    try(InputStream is = file.getInputStream()){
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      DataFormatter formatter = new DataFormatter();

      for(int i=1; i<= sheet.getLastRowNum(); i++){
        Row row = sheet.getRow(i);
        if(isRowEmpty(row)) continue;

        try {
            String email = formatter.formatCellValue(row.getCell(0)).trim();
            if (email.isEmpty()) {
                errors.add("Row " + (i + 1) + ": User Email (column 1) is required.");
                continue;
            }
            Optional<User> userOpt = userRepo.findByEmail(email);
            if(userOpt.isEmpty()){
                errors.add("Row " + (i + 1) + ": User with email '" + email + "' not found.");
                continue; 
            }
            if (empRepo.findByUserId(userOpt.get().getId()).isPresent()) {
                errors.add("Row " + (i + 1) + ": An employee profile already exists for user '" + email + "'.");
                continue;
            }

            String employeeCode = formatter.formatCellValue(row.getCell(1)).trim();
            if (employeeCode.isEmpty()) {
                errors.add("Row " + (i + 1) + ": Employee Code (column 2) is required.");
                continue;
            }
            if (empRepo.findByEmployeeCode(employeeCode).isPresent()) {
                errors.add("Row " + (i + 1) + ": Employee with code '" + employeeCode + "' already exists.");
                continue;
            }

            Employee employee = new Employee();
            employee.setUser(userOpt.get());
            employee.setEmployeeCode(employeeCode);
            employee.setFirstName(formatter.formatCellValue(row.getCell(2)));
            employee.setMiddleName(formatter.formatCellValue(row.getCell(3)));
            employee.setLastName(formatter.formatCellValue(row.getCell(4)));
            employee.setEmailWork(formatter.formatCellValue(row.getCell(5)));
            employee.setEmailPersonal(formatter.formatCellValue(row.getCell(6)));
            employee.setPhonePrimary(formatter.formatCellValue(row.getCell(7)));

            try {
                employee.setDob(row.getCell(9).getLocalDateTimeCellValue().toLocalDate());
            } catch (Exception dateEx) {
                errors.add("Row " + (i + 1) + ": Invalid date format for DOB (column 10). Expected a date format.");
                continue;
            }

            employee.setGender(Gender.valueOf(formatter.formatCellValue(row.getCell(10)).toUpperCase()));
            employee.setMartialStatus(MartialStatus.valueOf(formatter.formatCellValue(row.getCell(11)).toUpperCase()));
            employee.setStatus(EmployeeStatus.valueOf(formatter.formatCellValue(row.getCell(16)).toUpperCase()));

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            employee.setCreatedBy(username);
            employee.setUpdatedBy(username);
            employee.setCreatedAt(LocalDateTime.now());
            employee.setUpdatedAt(LocalDateTime.now());
            employeesToSave.add(employee);
        
        } catch (IllegalArgumentException e) {
            errors.add("Error on row " + (i + 1) + ": Invalid enum value provided. " + e.getMessage());
        } catch (Exception e) {
            errors.add("Error processing row " + (i + 1) + ": " + e.getMessage());
        }
      }

      if (!errors.isEmpty()) {
          return ResponseEntity.badRequest().body("File processing failed with errors:\n" + String.join("\n", errors));
      }
      empRepo.saveAll(employeesToSave);
      return ResponseEntity.ok("Bulk employees added successfully. " + employeesToSave.size() + " employees created.");
      } catch (Exception e) {
          return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
      }
  }

  @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  @Transactional(readOnly = true)
  public List<Employee> all() {
      return empRepo.findAll();
  }

  @GetMapping("/{employeeCode}")
  @Transactional(readOnly = true)
  public ResponseEntity<Employee> fetchByEmployeeCode(@PathVariable String employeeCode){
    return empRepo.findByEmployeeCode(employeeCode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-user-email/{email}")
  @Transactional(readOnly = true)
  public ResponseEntity<Employee> fetchByUserEmail(@PathVariable String email){
    return userRepo.findByEmail(email)
            .flatMap(user -> empRepo.findByUserId(user.getId()))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/by-gender/{gender}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  @Transactional(readOnly = true)
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  @Transactional(readOnly = true)
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  @Transactional(readOnly = true)
  public ResponseEntity<?> fetchByStatus(@PathVariable String status){
    try {
        EmployeeStatus statusEnum = EmployeeStatus.valueOf(status.toUpperCase());
        List<Employee> employees = empRepo.findByStatus(statusEnum);
        return ResponseEntity.ok(employees);
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("Invalid status value: " + status);
    }
  }
  
  @PostMapping("/{employeeCode}/photo")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  public ResponseEntity<?> uploadPhoto(@PathVariable String employeeCode, @RequestParam("file") MultipartFile file) {
      String contentType = file.getContentType();
      if (contentType == null || !contentType.startsWith("image/")) {
          return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Invalid file type. Please upload an image (e.g., JPEG, PNG).");
      }

      return empRepo.findByEmployeeCode(employeeCode)
              .map(employee -> {
                  // Optional: Delete old photo if it exists
                  if (employee.getPhotoPath() != null && !employee.getPhotoPath().isEmpty()) {
                      fileStorageService.deleteFile(employee.getPhotoPath());
                  }
                  
                  String fileName = fileStorageService.storeFile(file, employeeCode);
                  employee.setPhotoPath(fileName);
                  
                  String username = SecurityContextHolder.getContext().getAuthentication().getName();
                  employee.setUpdatedBy(username);
                  employee.setUpdatedAt(LocalDateTime.now());
                  
                  Employee updatedEmployee = empRepo.save(employee);
                  return ResponseEntity.ok(updatedEmployee);
              })
              .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{employeeCode}/photo")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Resource> getPhoto(@PathVariable String employeeCode) {
      return (ResponseEntity<Resource>) empRepo.findByEmployeeCode(employeeCode)
              .map(employee -> {
                  if (employee.getPhotoPath() == null || employee.getPhotoPath().isEmpty()) {
                      return ResponseEntity.notFound().build();
                  }
                  Resource resource = fileStorageService.loadFileAsResource(employee.getPhotoPath());
                  String contentType = "application/octet-stream";
                  try {
                      Path path = resource.getFile().toPath();
                      contentType = java.nio.file.Files.probeContentType(path);
                  } catch (IOException ex) {
                      logger.warn("Could not determine content type for photo: {}", resource.getFilename(), ex);
                  }
                  return ResponseEntity.ok()
                          .contentType(MediaType.parseMediaType(contentType))
                          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                          .body(resource);
              })
              .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{employeeCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
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
    // User and EmployeeCode are identifiers and should not be changed here.
    employee.setEmployeeCode(request.getEmployeeCode());
    employee.setFirstName(request.getFirstName());
    employee.setMiddleName(request.getMiddleName());
    employee.setLastName(request.getLastName());
    employee.setEmailWork(request.getEmailWork());
    employee.setEmailPersonal(request.getEmailPersonal());
    employee.setPhonePrimary(request.getPhonePrimary());
    employee.setDob(request.getDob());
    employee.setGender(request.getGender());
    employee.setMartialStatus(request.getMartialStatus());
    employee.setStatus(request.getStatus());
    employee.setPhotoPath(request.getPhotoPath());

    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    employee.setUpdatedBy(username);
    employee.setUpdatedAt(LocalDateTime.now());
  }

  private boolean isRowEmpty(Row row) {
    if (row == null) {
        return true;
    }
    DataFormatter formatter = new DataFormatter();
    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
        Cell cell = row.getCell(c);
        if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
            return false;
        }
    }
    return true;
  }

}
