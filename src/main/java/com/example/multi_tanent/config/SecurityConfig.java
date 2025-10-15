package com.example.multi_tanent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.multi_tanent.security.JwtAuthFilter;
import com.example.multi_tanent.security.JwtUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import jakarta.servlet.ServletException;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

  // @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
  // private String[] allowedOrigins;


  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth -> auth
      .requestMatchers(
        "/", "/index.html",
        "/dist/**",          // your current build is under static/dist
        "/favicon.ico",
        "/css/**", "/js/**", "/assets/**", "/images/**",
        "/static/**",        // in case files end up here
        "/uploads/**"        // Allow public access to uploaded files
          ).permitAll()
        .requestMatchers("/api/master/auth/login").permitAll()
        .requestMatchers("/api/auth/login").permitAll()
        .requestMatchers("/api/master/tenant-requests/register").permitAll()
        .requestMatchers("/api/biometric-punch/**").permitAll() // Allow device punches
        .requestMatchers("/public/products/**").permitAll() // Allow public access to product info via QR code

        // Master Admin Endpoints
        .requestMatchers("/api/master/tenant-requests/**").authenticated()
        .requestMatchers("/api/provision").hasRole("MASTER_ADMIN") // Allow provisioning for master admins
        .requestMatchers("/api/master/tenants/**").hasRole("MASTER_ADMIN")
        .requestMatchers("/api/master/users/**").authenticated()

        // Shared/Base Tenant Endpoints
        .requestMatchers("/api/users/**").authenticated()
        .requestMatchers("/api/locations/**").authenticated()
        .requestMatchers("/api/base/categories/**").authenticated()

        // HRMS Module Endpoints
        .requestMatchers("/api/employees/**", "/api/departments/**", "/api/designations/**", "/api/job-details/**", "/api/jobBands/**", "/api/nationalities/**").authenticated()
        .requestMatchers("/api/attendance-records/**", "/api/time-attendence/**", "/api/time-types/**", "/api/work-types/**", "/api/shift-types/**", "/api/shift-policies/**", "/api/weekly-off-policies/**", "/api/attendance-policies/**", "/api/attendance-capturing-policies/**").authenticated()
        .requestMatchers("/api/leaves/**", "/api/leave-requests/**", "/api/leave-groups/**", "/api/leave-types/**", "/api/leave-policies/**", "/api/leave-allocations/**", "/api/leave-balances/**", "/api/leave-encashment-requests/**", "/api/leave-approvals/**", "/api/holiday-policies/**", "/api/holidays/**").authenticated()
        .requestMatchers("/api/payrolls/**", "/api/payroll-runs/**", "/api/payslips/**", "/api/payroll-settings/**", "/api/salary-components/**", "/api/salary-structures/**", "/api/salary-structure-components/**", "/api/statutory-rules/**").authenticated()
        .requestMatchers("/api/loan-products/**", "/api/employee-loans/**", "/api/expenses/**", "/api/employee-bank-accounts/**").authenticated()
        .requestMatchers("/api/employee-documents/**", "/api/employee-profiles/**", "/api/company-info/**", "/api/company-locations/**", "/api/company-bank-accounts/**").authenticated()

        // POS Module Endpoints
        .requestMatchers("/api/pos/**").authenticated()
        
        .anyRequest().denyAll()
      )
      .addFilterBefore(spaRedirectFilter(), ChannelProcessingFilter.class)
      .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
      .sessionManagement(sm -> sm.sessionCreationPolicy(
        org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  public Filter spaRedirectFilter() {
      return (servletRequest, servletResponse, filterChain) -> {
          HttpServletRequest request = (HttpServletRequest) servletRequest;
          String path = request.getRequestURI();

          // Forward to index.html if it's not an API call and not a static file
          if (!path.startsWith("/api") && !path.contains(".") && path.matches("/(.*)")) {
              request.getRequestDispatcher("/index.html").forward(servletRequest, servletResponse);
              return;
          }

          filterChain.doFilter(servletRequest, servletResponse);
      };
  }

 @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173", "https://gtrhrms.netlify.app", "http://localhost:8080")); // React app URL
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true); // If using cookies or auth headers

        config.setExposedHeaders(Arrays.asList("Content-Disposition")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
