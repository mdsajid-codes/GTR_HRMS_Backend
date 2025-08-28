package com.example.multi_tanent.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.*;

public class JwtUtil {
  private final Key key = Keys.hmacShaKeyFor("uD2hrz4sQ3Md8XnpFeL3kbsH1q6NXaFoGdO2ZwWtbNY".getBytes());

  public String generateToken(String username, String tenantId, Collection<String> roles) {
    return Jwts.builder()
      .setSubject(username)
      .claim("tenantId", tenantId)
      .claim("roles", roles)
      .setIssuedAt(new Date())
      .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
      .signWith(key, SignatureAlgorithm.HS256)
      .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }
}
