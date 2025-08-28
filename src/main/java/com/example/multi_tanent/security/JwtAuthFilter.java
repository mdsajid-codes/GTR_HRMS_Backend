package com.example.multi_tanent.security;

import com.example.multi_tanent.config.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
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
    final String authHeader = req.getHeader("Authorization");

    try {
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
        final String token = authHeader.substring(7);
        try {
          Jws<Claims> claimsJws = jwt.parse(token);
          Claims claims = claimsJws.getBody();
          String username = claims.getSubject();
          String tenantId = claims.get("tenantId", String.class);
          @SuppressWarnings("unchecked")
          List<String> roles = (List<String>) claims.get("roles");

          // Set tenant for this request thread
          TenantContext.setTenantId(tenantId);

          // Set security context for Spring Security
          var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
          var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authToken);
        } catch (JwtException e) {
          logger.warn("JWT Authentication error: " + e.getMessage());
          SecurityContextHolder.clearContext();
          res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Error: Invalid or expired token.");
          return; // Stop the filter chain
        }
      }
      chain.doFilter(req, res);
    } finally {
      // Crucially, clear the tenant context to prevent leaks in the thread pool.
      TenantContext.clear();
    }
  }
}
