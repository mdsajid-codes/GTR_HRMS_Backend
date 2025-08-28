package com.example.multi_tanent.master.controller;

import com.example.multi_tanent.master.entity.MasterUser;
import com.example.multi_tanent.master.repository.MasterUserRepository;
import com.example.multi_tanent.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/master/auth")
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
  public String login(@RequestParam String username, @RequestParam String password) {
    var u = repo.findByUsername(username).orElseThrow();
    if (!encoder.matches(password, u.getPasswordHash())) throw new RuntimeException("bad creds");
    // tenantId = "master" for master admin tokens
    return jwt.generateToken(username, "master", java.util.List.of("MASTER_ADMIN"));
  }
}