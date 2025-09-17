package com.example.multi_tanent.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1) // Ensure this runs before other security filters
public class BiometricTenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(BiometricTenantFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/biometric-punch/")) {
            String[] pathParts = path.split("/");
            if (pathParts.length > 3) {
                String tenantId = pathParts[3]; // e.g., /api/biometric-punch/{tenantId}
                TenantContext.setTenantId(tenantId);
                logger.info("BiometricTenantFilter: Set tenant context to '{}' for request: {}", tenantId, path);
            }
        }
        filterChain.doFilter(request, response);
    }
}