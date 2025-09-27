package com.example.multi_tanent.pos.controller;

import com.example.multi_tanent.pos.dto.PosUserRequest;
import com.example.multi_tanent.pos.entity.PosUser;
import com.example.multi_tanent.pos.service.PosUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pos/users")
@CrossOrigin(origins = "*")
public class PosUserController {
    private final PosUserService posUserService;

    public PosUserController(PosUserService posUserService) {
        this.posUserService = posUserService;
    }

    @PostMapping
    @PreAuthorize("hasRole('POS_ADMIN')")
    public ResponseEntity<?> createPosUser(@Valid @RequestBody PosUserRequest userRequest) {
        try {
            PosUser createdUser = posUserService.createPosUser(userRequest);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdUser.getId())
                    .toUri();
            return ResponseEntity.created(location).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('POS_ADMIN', 'POS_MANAGER')")
    public ResponseEntity<List<PosUser>> getAllPosUsers() {
        return ResponseEntity.ok(posUserService.getAllPosUsersForCurrentTenant());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('POS_ADMIN', 'POS_MANAGER')")
    public ResponseEntity<PosUser> getPosUserById(@PathVariable Long id) {
        return posUserService.getPosUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('POS_ADMIN')")
    public ResponseEntity<?> updatePosUser(@PathVariable Long id, @Valid @RequestBody PosUserRequest userRequest) {
        try {
            PosUser updatedUser = posUserService.updatePosUser(id, userRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('POS_ADMIN')")
    public ResponseEntity<Void> deletePosUser(@PathVariable Long id) {
        posUserService.deletePosUser(id);
        return ResponseEntity.noContent().build();
    }
}
