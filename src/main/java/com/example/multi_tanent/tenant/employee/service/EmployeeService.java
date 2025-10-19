package com.example.multi_tanent.tenant.employee.service;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.master.entity.MasterTenant;
import com.example.multi_tanent.master.enums.Role;
import com.example.multi_tanent.master.repository.MasterTenantRepository;
import com.example.multi_tanent.spersusers.enitity.Employee;
import com.example.multi_tanent.spersusers.enitity.Location;
import com.example.multi_tanent.spersusers.enitity.User;
import com.example.multi_tanent.spersusers.repository.LocationRepository;
import com.example.multi_tanent.spersusers.repository.UserRepository;
import com.example.multi_tanent.tenant.employee.dto.EmployeeRequest;
import com.example.multi_tanent.tenant.employee.entity.EmployeeProfile;
import com.example.multi_tanent.tenant.employee.entity.JobDetails;
import com.example.multi_tanent.tenant.employee.entity.TimeAttendence;
import com.example.multi_tanent.tenant.employee.enums.EmployeeStatus;
import com.example.multi_tanent.tenant.employee.enums.Gender;
import com.example.multi_tanent.tenant.employee.repository.EmployeeProfileRepository;
import com.example.multi_tanent.tenant.employee.repository.EmployeeRepository;
import com.example.multi_tanent.tenant.employee.repository.JobDetailsRepository;
import com.example.multi_tanent.tenant.employee.repository.TimeAttendenceRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(transactionManager = "tenantTx")
public class EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository empRepo;
    private final UserRepository userRepo;
    private final TenantRepository tenantRepo;
    private final EmployeeProfileRepository employeeProfileRepo;
    private final JobDetailsRepository jobDetailsRepo;
    private final TimeAttendenceRepository timeAttendenceRepo;
    private final PasswordEncoder passwordEncoder;
    private final LocationRepository locationRepository;
    private final MasterTenantRepository masterTenantRepository;

    public EmployeeService(EmployeeRepository empRepo, UserRepository userRepo, TenantRepository tenantRepo,
                           EmployeeProfileRepository employeeProfileRepo, JobDetailsRepository jobDetailsRepo,
                           TimeAttendenceRepository timeAttendenceRepo, PasswordEncoder passwordEncoder,
                           LocationRepository locationRepository, MasterTenantRepository masterTenantRepository) {
        this.empRepo = empRepo;
        this.userRepo = userRepo;
        this.tenantRepo = tenantRepo;
        this.employeeProfileRepo = employeeProfileRepo;
        this.jobDetailsRepo = jobDetailsRepo;
        this.timeAttendenceRepo = timeAttendenceRepo;
        this.passwordEncoder = passwordEncoder;
        this.locationRepository = locationRepository;
        this.masterTenantRepository = masterTenantRepository;
    }

    public Employee registerEmployee(EmployeeRequest request) {
        String tenantId = TenantContext.getTenantId();
        MasterTenant masterTenant = masterTenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Master tenant record not found. Cannot enforce subscription limits."));

        Integer employeeLimit = masterTenant.getHrmsAccessCount();
        if (employeeLimit != null) {
            long currentEmployeeCount = empRepo.count();
            if (currentEmployeeCount >= employeeLimit) {
                throw new IllegalStateException("HRMS employee limit of " + employeeLimit + " has been reached for your subscription.");
            }
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User with email '" + request.getEmail() + "' not found."));

        if (empRepo.findByEmployeeCode(request.getEmployeeCode()).isPresent()) {
            throw new IllegalArgumentException("Employee with code '" + request.getEmployeeCode() + "' already exists.");
        }

        if (empRepo.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("An employee profile already exists for this user.");
        }

        Employee e = new Employee();
        updateEmployeeFromRequest(e, request);
        e.setUser(user);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        e.setCreatedBy(username);
        e.setCreatedAt(LocalDateTime.now());

        return empRepo.save(e);
    }

    public String bulkAddEmployees(MultipartFile file) throws java.io.IOException {
        List<String> errors = new ArrayList<>();
        String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        String tenantId = TenantContext.getTenantId();
        MasterTenant masterTenant = masterTenantRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Master tenant record not found. Cannot enforce subscription limits."));
        Integer employeeLimit = masterTenant.getHrmsAccessCount();
        long currentEmployeeCount = empRepo.count();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            int newEmployeeCount = sheet.getLastRowNum(); // Assuming row 0 is header
            if (employeeLimit != null && (currentEmployeeCount + newEmployeeCount) > employeeLimit) {
                throw new IllegalStateException("Bulk import failed. This import would exceed your subscription limit of " + employeeLimit + " HRMS employees.");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row)) continue;

                try {
                    String userEmail = formatter.formatCellValue(row.getCell(0)).trim();
                    String employeeCode = formatter.formatCellValue(row.getCell(1)).trim();

                    if (userEmail.isEmpty() || employeeCode.isEmpty()) {
                        errors.add("Row " + (i + 1) + ": User Email (col 1) and Employee Code (col 2) are required.");
                        continue;
                    }

                    if (userRepo.findByEmail(userEmail).isPresent() || empRepo.findByEmployeeCode(employeeCode).isPresent()) {
                        errors.add("Row " + (i + 1) + ": User email or Employee code already exists.");
                        continue;
                    }

                    User user = new User();
                    user.setTenant(tenantRepo.findAll().stream().findFirst().orElseThrow());
                    user.setName(formatter.formatCellValue(row.getCell(2)).trim());
                    user.setEmail(userEmail);
                    user.setPasswordHash(passwordEncoder.encode(formatter.formatCellValue(row.getCell(3)).trim()));
                    user.setRoles(Arrays.stream(formatter.formatCellValue(row.getCell(4)).split(","))
                            .map(String::trim).map(String::toUpperCase).map(Role::valueOf).collect(Collectors.toSet()));
                    user.setIsActive(true);
                    user.setIsLocked(false);
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    User savedUser = userRepo.save(user);

                    Employee employee = new Employee();
                    employee.setUser(savedUser);
                    employee.setEmployeeCode(employeeCode);
                    employee.setFirstName(formatter.formatCellValue(row.getCell(5)));
                    employee.setLastName(formatter.formatCellValue(row.getCell(6)));
                    employee.setEmailWork(formatter.formatCellValue(row.getCell(7)));
                    employee.setPhonePrimary(formatter.formatCellValue(row.getCell(8)));
                    employee.setDob(getLocalDateFromCell(row.getCell(9)));
                    String locationIdStr = formatter.formatCellValue(row.getCell(10));
                    if (!locationIdStr.isEmpty()) {
                        locationRepository.findById(Long.parseLong(locationIdStr)).ifPresent(employee::setLocation);
                    }
                    employee.setGender(Gender.valueOf(formatter.formatCellValue(row.getCell(11)).toUpperCase()));
                    employee.setStatus(EmployeeStatus.ACTIVE);
                    employee.setCreatedBy(loggedInUsername);
                    employee.setUpdatedBy(loggedInUsername);
                    employee.setCreatedAt(LocalDateTime.now());
                    employee.setUpdatedAt(LocalDateTime.now());
                    Employee savedEmployee = empRepo.save(employee);

                    EmployeeProfile profile = new EmployeeProfile();
                    profile.setEmployee(savedEmployee);
                    profile.setAddress(formatter.formatCellValue(row.getCell(12)));
                    profile.setCity(formatter.formatCellValue(row.getCell(13)));
                    profile.setState(formatter.formatCellValue(row.getCell(14)));
                    profile.setCountry(formatter.formatCellValue(row.getCell(15)));
                    profile.setPostalCode(formatter.formatCellValue(row.getCell(16)));
                    profile.setBankName(formatter.formatCellValue(row.getCell(17)));
                    profile.setBankAccountNumber(formatter.formatCellValue(row.getCell(18)));
                    profile.setIfscCode(formatter.formatCellValue(row.getCell(19)));
                    employeeProfileRepo.save(profile);

                    JobDetails jobDetails = new JobDetails();
                    jobDetails.setEmployee(savedEmployee);
                    jobDetails.setDesignation(formatter.formatCellValue(row.getCell(20)));
                    jobDetails.setDepartment(formatter.formatCellValue(row.getCell(21)));
                    jobDetails.setDateOfJoining(getLocalDateFromCell(row.getCell(22)));
                    jobDetails.setReportsTo(formatter.formatCellValue(row.getCell(23)));
                    jobDetailsRepo.save(jobDetails);

                    TimeAttendence timeAttendence = new TimeAttendence();
                    timeAttendence.setEmployee(savedEmployee);
                    // timeAttendence.setLeaveGroup(formatter.formatCellValue(row.getCell(24))); // This needs to be updated to use IDs
                    timeAttendenceRepo.save(timeAttendence);

                } catch (Exception e) {
                    logger.error("Error processing row " + (i + 1), e);
                    errors.add("Row " + (i + 1) + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("File processing failed with errors:\n" + String.join("\n", errors));
        }
        return "Bulk import process completed successfully.";
    }

    public void updateEmployeeFromRequest(Employee employee, EmployeeRequest request) {
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

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId()).orElse(null);
            employee.setLocation(location);
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        employee.setUpdatedBy(username);
        employee.setUpdatedAt(LocalDateTime.now());
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        DataFormatter formatter = new DataFormatter();
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private LocalDate getLocalDateFromCell(Cell cell) {
        if (cell == null) return null;
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (IllegalStateException | NumberFormatException e) {
            try {
                return LocalDate.parse(cell.getStringCellValue());
            } catch (Exception ex) {
                return null;
            }
        }
    }
}