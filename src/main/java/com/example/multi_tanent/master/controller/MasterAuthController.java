package com.example.multi_tanent.master.controller;

import com.example.multi_tanent.tenant.tenantDto.LoginResponse;
import com.example.multi_tanent.master.dto.MasterAuthRequest;
import com.example.multi_tanent.master.entity.MasterUser;
import com.example.multi_tanent.master.repository.MasterUserRepository;
import com.example.multi_tanent.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master/auth")
@CrossOrigin(origins = "*")
public class MasterAuthController {
  private final MasterUserRepository repo;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  public MasterAuthController(MasterUserRepository repo, PasswordEncoder encoder, JwtUtil jwt) {
    this.repo = repo; this.encoder = encoder; this.jwt = jwt;
  }

  @PostMapping("/register")
  @Transactional
  public String register(@RequestParam String username, @RequestParam String password) {
    if (repo.findByUsername(username).isPresent()) return "exists";
    MasterUser u = new MasterUser();
    u.setUsername(username);
    u.setPasswordHash(encoder.encode(password));
    repo.save(u);
    return "ok";
  }

  @PostMapping("/login")
  public LoginResponse login(@RequestBody MasterAuthRequest masterAuthRequest) {
    var u = repo.findByUsername(masterAuthRequest.getUsername()).orElseThrow();
    if (!encoder.matches(masterAuthRequest.getPassword(), u.getPasswordHash())) throw new RuntimeException("bad creds");
    // tenantId = "master" for master admin tokens
    List<String> roles = List.of("MASTER_ADMIN");
    String token = jwt.generateToken(masterAuthRequest.getUsername(), "master", roles);
    return new LoginResponse(token, roles);
  }
}