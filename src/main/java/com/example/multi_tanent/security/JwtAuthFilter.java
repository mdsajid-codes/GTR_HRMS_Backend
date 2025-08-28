package com.example.multi_tanent.security;

import com.example.multi_tanent.config.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtil jwt;

  public JwtAuthFilter(JwtUtil jwt) { this.jwt = jwt; }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String auth = req.getHeader("Authorization");
    try {
      if (auth != null && auth.startsWith("Bearer ")) {
        var claims = jwt.parse(auth.substring(7)).getBody();
        String username = claims.getSubject();
        String tenantId = claims.get("tenantId", String.class);
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");

        // set tenant
        TenantContext.setTenantId(tenantId);

        // set security
        var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
        var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
      chain.doFilter(req, res);
    } finally {
      TenantContext.clear();
      SecurityContextHolder.clearContext();
    }
  }
}
