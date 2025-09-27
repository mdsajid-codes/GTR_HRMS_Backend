package com.example.multi_tanent.pos.controller;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.pos.dto.PosAuthRequest;
import com.example.multi_tanent.pos.dto.PosLoginResponse;
import com.example.multi_tanent.pos.entity.PosUser;
import com.example.multi_tanent.pos.repository.PosUserRepository;
import com.example.multi_tanent.pos.repository.TenantRepository;
import com.example.multi_tanent.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/auth")
@CrossOrigin(origins = "*")
public class PosAuthController {
    private final PosUserRepository posUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final JwtUtil jwtUtil;

    public PosAuthController(PosUserRepository posUserRepository,
                             PasswordEncoder passwordEncoder,
                             TenantRepository tenantRepository, JwtUtil jwtUtil) {
        this.posUserRepository = posUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody PosAuthRequest authRequest) {
        try {
            TenantContext.setTenantId(authRequest.getTenantId());

            Long tenantId = tenantRepository.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new RuntimeException("Tenant not found for the given ID."))
                    .getId();
            PosUser user = posUserRepository.findByEmailAndTenantId(authRequest.getEmail(), tenantId)
                    .orElseThrow(() -> new RuntimeException("Invalid username or password."));

            if (!passwordEncoder.matches(authRequest.getPassword(), user.getPasswordHash())) {
                throw new RuntimeException("Invalid username or password.");
            }

            List<String> roles = List.of(user.getRole().name());
            String token = jwtUtil.generateToken(user.getEmail(), authRequest.getTenantId(), roles);
            Long storeId = (user.getStore() != null) ? user.getStore().getId() : null;

            return ResponseEntity.ok(new PosLoginResponse(token, roles, user.getEmail(), user.getDisplayName(), storeId));
        } finally {
            TenantContext.clear();
        }
    }
}
