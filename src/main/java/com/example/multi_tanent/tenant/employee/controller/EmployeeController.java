package com.example.multi_tanent.tenant.employee.controller;

import com.example.multi_tanent.master.enums.Role;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.User;
import com.example.multi_tanent.spersusers.repository.UserRepository;
import com.example.multi_tanent.tenant.employee.dto.EmployeeRequest;
import com.example.multi_tanent.tenant.employee.entity.EmployeeProfile;
import com.example.multi_tanent.tenant.employee.entity.JobDetails;
import com.example.multi_tanent.tenant.employee.entity.TimeAttendence;
import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.enums.Gender;
import com.example.multi_tanent.tenant.employee.enums.MartialStatus;
import com.example.multi_tanent.tenant.employee.repository.*;
import com.example.multi_tanent.tenant.service.FileStorageService;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin (origins = "*")
@Transactional(transactionManager = "tenantTx")
public class EmployeeController {
  private final EmployeeRepository empRepo;
  private final UserRepository userRepo;
  private final TenantRepository tenantRepo;
  private final EmployeeProfileRepository employeeProfileRepo;
  private final JobDetailsRepository jobDetailsRepo;
  private final TimeAttendenceRepository timeAttendenceRepo;
  private final PasswordEncoder passwordEncoder;
  private final FileStorageService fileStorageService;
  private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);


  public EmployeeController(EmployeeRepository empRepo,
                            UserRepository userRepo,
                            TenantRepository tenantRepo,
                            EmployeeProfileRepository employeeProfileRepo,
                            JobDetailsRepository jobDetailsRepo,
                            TimeAttendenceRepository timeAttendenceRepo,
                            PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
    this.empRepo = empRepo;
    this.userRepo = userRepo;
    this.tenantRepo = tenantRepo;
    this.employeeProfileRepo = employeeProfileRepo;
    this.jobDetailsRepo = jobDetailsRepo;
    this.timeAttendenceRepo = timeAttendenceRepo;
    this.passwordEncoder = passwordEncoder;
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

  @GetMapping("/bulk-template")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  public ResponseEntity<byte[]> downloadBulkAddTemplate() throws IOException {
      String[] headers = {
              "User Email*", "Employee Code*", "Full Name*", "Password*", "Roles (comma-separated)*",
              "First Name*", "Last Name*", "Work Email", "Primary Phone", "Date of Birth (YYYY-MM-DD)",
              "Gender (MALE/FEMALE/OTHER)", "Address", "City", "State", "Country", "Postal Code",
              "Bank Name", "Bank Account Number", "IFSC Code", "Designation", "Department",
              "Date of Joining (YYYY-MM-DD)", "Reports To (Manager's Employee Code)", "Leave Group Name"
      };

      try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          Sheet sheet = workbook.createSheet("Employees");

          // Create header row with bold font
          Row headerRow = sheet.createRow(0);
          CellStyle headerCellStyle = workbook.createCellStyle();
          Font font = workbook.createFont();
          font.setBold(true);
          headerCellStyle.setFont(font);

          for (int col = 0; col < headers.length; col++) {
              Cell cell = headerRow.createCell(col);
              cell.setCellValue(headers[col]);
              cell.setCellStyle(headerCellStyle);
              sheet.autoSizeColumn(col);
          }

          // Add a second sheet with instructions and valid values
          Sheet instructionsSheet = workbook.createSheet("Instructions");
          Row infoRow1 = instructionsSheet.createRow(0);
          infoRow1.createCell(0).setCellValue("Instructions for Bulk Employee Upload");

          instructionsSheet.createRow(2).createCell(0).setCellValue("Columns marked with * are mandatory.");
          instructionsSheet.createRow(3).createCell(0).setCellValue("Date Format: Please use YYYY-MM-DD for all date fields.");
          instructionsSheet.createRow(4).createCell(0).setCellValue("Gender: Valid values are MALE, FEMALE, OTHER (case-insensitive).");
          instructionsSheet.createRow(5).createCell(0).setCellValue("Roles: Provide a comma-separated list of roles (e.g., EMPLOYEE,MANAGER).");
          instructionsSheet.createRow(6).createCell(0).setCellValue("Leave Group: Ensure the Leave Group Name exists in the system before uploading.");

          for (int i = 0; i < 2; i++) {
              instructionsSheet.autoSizeColumn(i);
          }

          workbook.write(out);

          HttpHeaders responseHeaders = new HttpHeaders();
          responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
          responseHeaders.setContentDispositionFormData("attachment", "employee_bulk_upload_template.xlsx");

          return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
      }
  }


  @PostMapping("/bulkEmployees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','HRMS_ADMIN','HR','MANAGER')")
  public ResponseEntity<String> addBulkEmployees(@RequestParam("file") MultipartFile file){
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("Cannot process an empty file.");
    }

    List<String> errors = new ArrayList<>();

    try(InputStream is = file.getInputStream()){
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      DataFormatter formatter = new DataFormatter();

      String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();

      for(int i=1; i<= sheet.getLastRowNum(); i++){
        Row row = sheet.getRow(i);
        if(isRowEmpty(row)) continue;

        try {
            // --- User and Employee Validation ---
            String userEmail = formatter.formatCellValue(row.getCell(0)).trim();
            String employeeCode = formatter.formatCellValue(row.getCell(1)).trim();

            if (userEmail.isEmpty() || employeeCode.isEmpty()) {
                errors.add("Row " + (i + 1) + ": User Email (col 1) and Employee Code (col 2) are required.");
                continue;
            }

            if (userRepo.findByEmail(userEmail).isPresent()) {
                errors.add("Row " + (i + 1) + ": User with email '" + userEmail + "' already exists.");
                continue;
            }
            if (empRepo.findByEmployeeCode(employeeCode).isPresent()) {
                errors.add("Row " + (i + 1) + ": Employee with code '" + employeeCode + "' already exists.");
                continue;
            }

            // --- Create User ---
            User user = new User();
            user.setTenant(tenantRepo.findAll().stream().findFirst().orElseThrow());
            user.setName(formatter.formatCellValue(row.getCell(2)).trim()); // Full Name for User
            user.setEmail(userEmail);
            user.setPasswordHash(passwordEncoder.encode(formatter.formatCellValue(row.getCell(3)).trim()));
            user.setRoles(Arrays.stream(formatter.formatCellValue(row.getCell(4)).split(","))
                    .map(String::trim)
                    .map(String::toUpperCase) // Convert to uppercase to match enum names
                    .map(Role::valueOf)       // Convert string to Role enum
                    .collect(Collectors.toSet()));
            user.setIsActive(true);
            user.setIsLocked(false);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepo.save(user);

            // --- Create Employee ---
            Employee employee = new Employee();
            employee.setUser(savedUser);
            employee.setEmployeeCode(employeeCode);
            employee.setFirstName(formatter.formatCellValue(row.getCell(5)));
            employee.setLastName(formatter.formatCellValue(row.getCell(6)));
            employee.setEmailWork(formatter.formatCellValue(row.getCell(7)));
            employee.setPhonePrimary(formatter.formatCellValue(row.getCell(8)));
            employee.setDob(getLocalDateFromCell(row.getCell(9)));
            employee.setGender(Gender.valueOf(formatter.formatCellValue(row.getCell(10)).toUpperCase()));
            employee.setStatus(EmployeeStatus.ACTIVE);
            employee.setCreatedBy(loggedInUsername);
            employee.setUpdatedBy(loggedInUsername);
            employee.setCreatedAt(LocalDateTime.now());
            employee.setUpdatedAt(LocalDateTime.now());
            Employee savedEmployee = empRepo.save(employee);

            // --- Create EmployeeProfile ---
            EmployeeProfile profile = new EmployeeProfile();
            profile.setEmployee(savedEmployee);
            profile.setAddress(formatter.formatCellValue(row.getCell(11)));
            profile.setCity(formatter.formatCellValue(row.getCell(12)));
            profile.setState(formatter.formatCellValue(row.getCell(13)));
            profile.setCountry(formatter.formatCellValue(row.getCell(14)));
            profile.setPostalCode(formatter.formatCellValue(row.getCell(15)));
            profile.setBankName(formatter.formatCellValue(row.getCell(16)));
            profile.setBankAccountNumber(formatter.formatCellValue(row.getCell(17)));
            profile.setIfscCode(formatter.formatCellValue(row.getCell(18)));
            employeeProfileRepo.save(profile);

            // --- Create JobDetails ---
            JobDetails jobDetails = new JobDetails();
            jobDetails.setEmployee(savedEmployee);
            jobDetails.setDesignation(formatter.formatCellValue(row.getCell(19)));
            jobDetails.setDepartment(formatter.formatCellValue(row.getCell(20)));
            jobDetails.setDateOfJoining(getLocalDateFromCell(row.getCell(21)));
            jobDetails.setReportsTo(formatter.formatCellValue(row.getCell(22)));
            jobDetailsRepo.save(jobDetails);

            // --- Create TimeAttendence ---
            TimeAttendence timeAttendence = new TimeAttendence();
            timeAttendence.setEmployee(savedEmployee);
            timeAttendence.setLeaveGroup(formatter.formatCellValue(row.getCell(23)));
            timeAttendenceRepo.save(timeAttendence);

        } catch (IllegalArgumentException e) {
            errors.add("Row " + (i + 1) + ": Invalid enum value. " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error processing row " + (i + 1), e);
            errors.add("Row " + (i + 1) + ": " + e.getMessage());
        }
      }

      if (!errors.isEmpty()) {
          return ResponseEntity.badRequest().body("File processing failed with errors:\n" + String.join("\n", errors));
      }
      return ResponseEntity.ok("Bulk import process completed. Check logs for details.");
      } catch (Exception e) {
          logger.error("Failed to process bulk employee file", e);
          return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
      }
  }

    private LocalDate getLocalDateFromCell(Cell cell) {
        if (cell == null) return null;
        try {
            // Try to get it as a date first
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (IllegalStateException | NumberFormatException e) {
            // If that fails, try to parse it as a string
            try {
                return LocalDate.parse(cell.getStringCellValue());
            } catch (Exception ex) {
                return null;
            }
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
