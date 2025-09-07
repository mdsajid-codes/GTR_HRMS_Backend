// com/example/multi_tanent/tenant/controller/UserController.java
package com.example.multi_tanent.tenant.controller;

import com.example.multi_tanent.tenant.entity.User;
import com.example.multi_tanent.tenant.entity.enums.Role;
import com.example.multi_tanent.tenant.repository.UserRepository;
import com.example.multi_tanent.tenant.tenantDto.UserRegisterRequest;
import com.example.multi_tanent.tenant.tenantDto.UserResponse;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@org.springframework.transaction.annotation.Transactional(transactionManager = "tenantTx")
public class UserController {
  private final UserRepository repo;
  private final PasswordEncoder encoder;

  public UserController(UserRepository repo, PasswordEncoder encoder) {
    this.repo = repo; this.encoder = encoder;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')") // only these roles can create users
  public UserResponse create(@RequestBody UserRegisterRequest uRequest) {
    User user = new User();
    user.setName(uRequest.getName());
    user.setEmail(uRequest.getEmail());
    user.setPasswordHash(encoder.encode(uRequest.getPasswordHash()));
    user.setRoles(uRequest.getRoles());
    user.setIsActive(uRequest.getIsActive());
    user.setIsLocked(uRequest.getIsLocked());
    user.setLoginAttempts(uRequest.getLoginAttempts());
    user.setLastLoginAt(LocalDateTime.now());
    user.setLastLoginIp(uRequest.getLastLoginIp());
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    User savedUser = repo.save(user);
    return UserResponse.fromEntity(savedUser);
  }

  @PostMapping("/bulkUsers")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
  public ResponseEntity<String> addBulkUsers(@RequestParam("file") MultipartFile file){
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("Cannot process an empty file.");
    }

    List<User> usersToSave = new ArrayList<>();
    List<String> errors = new ArrayList<>();

    try(InputStream is = file.getInputStream()){
      Workbook workbook = WorkbookFactory.create(is);
      Sheet sheet = workbook.getSheetAt(0);
      DataFormatter formatter = new DataFormatter();
      
      for(int i=1; i<=sheet.getLastRowNum(); i++){
        Row row = sheet.getRow(i);
        if(isRowEmpty(row)) continue;

        try {
            // Assuming columns are: Name, Email, Password, Roles (comma-separated), IsActive, IsLocked, LoginAttempts, LastLoginIp
            String email = formatter.formatCellValue(row.getCell(1)).trim();
            if (email.isEmpty()) {
                errors.add("Row " + (i + 1) + ": Email is required.");
                continue;
            }
            if (repo.findByEmail(email).isPresent()) {
                errors.add("Row " + (i + 1) + ": User with email '" + email + "' already exists.");
                continue;
            }

            User user = new User();
            user.setEmail(email);
            user.setName(formatter.formatCellValue(row.getCell(0)));
            user.setPasswordHash(encoder.encode(formatter.formatCellValue(row.getCell(2))));

            String rolesString = formatter.formatCellValue(row.getCell(3));
            Set<Role> roles = Arrays.stream(rolesString.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(roleName -> {
                    try { return Role.valueOf(roleName); }
                    catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid role '" + roleName + "'"); }
                })
                .collect(Collectors.toSet());

            if (roles.isEmpty()) {
                errors.add("Row " + (i + 1) + ": At least one valid role is required.");
                continue;
            }
            user.setRoles(roles);

            user.setIsActive(Boolean.parseBoolean(formatter.formatCellValue(row.getCell(4))));
            user.setIsLocked(Boolean.parseBoolean(formatter.formatCellValue(row.getCell(5))));
            String loginAttemptsStr = formatter.formatCellValue(row.getCell(6));
            user.setLoginAttempts(loginAttemptsStr.isEmpty() ? 0 : Integer.parseInt(loginAttemptsStr));
            user.setLastLoginIp(formatter.formatCellValue(row.getCell(7)));
            user.setLastLoginAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            usersToSave.add(user);
        } catch (Exception e) {
            errors.add("Error on row " + (i + 1) + ": " + e.getMessage());
        }
      }

      if (!errors.isEmpty()) {
          return ResponseEntity.badRequest().body("File processing failed with errors:\n" + String.join("\n", errors));
      }

      repo.saveAll(usersToSave);
      return ResponseEntity.ok("Bulk users added successfully. " + usersToSave.size() + " users created.");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Failed to read the uploaded file: " + e.getMessage());
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
  public UserResponse update(@PathVariable Long id, @RequestBody UserRegisterRequest uRequest) {
    User user = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    user.setName(uRequest.getName());
    user.setEmail(uRequest.getEmail());
    // Only update password if a new one is provided
    if (uRequest.getPasswordHash() != null && !uRequest.getPasswordHash().isEmpty()) {
      user.setPasswordHash(encoder.encode(uRequest.getPasswordHash()));
    }
    user.setRoles(uRequest.getRoles());
    user.setIsActive(uRequest.getIsActive());
    user.setIsLocked(uRequest.getIsLocked());
    user.setLoginAttempts(uRequest.getLoginAttempts());
    user.setLastLoginIp(uRequest.getLastLoginIp());
    user.setUpdatedAt(LocalDateTime.now());
    User updatedUser = repo.save(user);
    return UserResponse.fromEntity(updatedUser);
  }


  @GetMapping
  @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
  public List<UserResponse> all() {
    return repo.findAll().stream()
            .map(UserResponse::fromEntity)
            .collect(Collectors.toList());
  }

  private boolean isRowEmpty(Row row) {
    if (row == null) {
        return true;
    }
    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
        Cell cell = row.getCell(c);
        if (cell != null && cell.getCellType() != CellType.BLANK) {
            return false;
        }
    }
    return true;
  }
}
