package com.example.multi_tanent.tenant.base.controller;

import com.example.multi_tanent.tenant.base.dto.TimeTypeRequest;
import com.example.multi_tanent.tenant.base.entity.TimeType;
import com.example.multi_tanent.tenant.base.repository.TimeTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/time-types")
@CrossOrigin(origins = "*")
@Transactional(transactionManager = "tenantTx")
public class TimeTypeController {

    private final TimeTypeRepository timeTypeRepository;

    public TimeTypeController(TimeTypeRepository timeTypeRepository) {
        this.timeTypeRepository = timeTypeRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<?> createTimeType(@RequestBody TimeTypeRequest request) {
        if (timeTypeRepository.findByCode(request.getCode()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A time type with the code '" + request.getCode() + "' already exists.");
        }
        if (timeTypeRepository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A time type with the name '" + request.getName() + "' already exists.");
        }

        TimeType timeType = new TimeType();
        timeType.setCode(request.getCode());
        timeType.setName(request.getName());

        TimeType savedTimeType = timeTypeRepository.save(timeType);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedTimeType.getId()).toUri();

        return ResponseEntity.created(location).body(savedTimeType);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TimeType>> getAllTimeTypes() {
        return ResponseEntity.ok(timeTypeRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TimeType> getTimeTypeById(@PathVariable Long id) {
        return timeTypeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<?> updateTimeType(@PathVariable Long id, @RequestBody TimeTypeRequest request) {
        Optional<TimeType> existingByCode = timeTypeRepository.findByCode(request.getCode());
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Another time type with the code '" + request.getCode() + "' already exists.");
        }
        Optional<TimeType> existingByName = timeTypeRepository.findByName(request.getName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Another time type with the name '" + request.getName() + "' already exists.");
        }

        return timeTypeRepository.findById(id)
                .map(timeType -> {
                    timeType.setCode(request.getCode());
                    timeType.setName(request.getName());
                    TimeType updatedTimeType = timeTypeRepository.save(timeType);
                    return ResponseEntity.ok(updatedTimeType);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','HR')")
    public ResponseEntity<Void> deleteTimeType(@PathVariable Long id) {
        return timeTypeRepository.findById(id)
                .map(timeType -> {
                    timeTypeRepository.delete(timeType);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
