package com.example.multi_tanent.tenant.controller;

import com.example.multi_tanent.config.TenantContext;
import com.example.multi_tanent.tenant.tenantDto.LoginResponse;
import com.example.multi_tanent.security.JwtUtil;
import com.example.multi_tanent.tenant.repository.UserRepository;
import com.example.multi_tanent.tenant.tenantDto.UserRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
    this.users = users; this.encoder = encoder; this.jwt = jwt;
  }

  // First tenant admin can be seeded or created by schema initializer.
  @PostMapping("/login")
  public LoginResponse login(@RequestBody UserRequest userRequest) {
    try {
      // For login, we must manually set the tenant context from the request
      TenantContext.setTenantId(userRequest.getTenantId());

      var u = users.findByEmail(userRequest.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));
      if (!encoder.matches(userRequest.getPassword(), u.getPasswordHash())) {
        throw new RuntimeException("Bad credentials");
      }
      List<String> roles = u.getRoles().stream().map(Enum::name).collect(Collectors.toList());
      String token = jwt.generateToken(userRequest.getEmail(), userRequest.getTenantId(), roles);
      return new LoginResponse(token, roles);
    } finally {
      TenantContext.clear(); // Always clear the context
    }
  }
}
