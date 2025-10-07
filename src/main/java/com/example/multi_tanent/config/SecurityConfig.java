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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.multi_tanent.security.JwtAuthFilter;
import com.example.multi_tanent.security.JwtUtil;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

  @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
  private String[] allowedOrigins;


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
        .requestMatchers("/api/master/tenant-requests/register").permitAll()
        .requestMatchers("/api/master/tenant-requests/**").authenticated()
        .requestMatchers("/api/provision").hasRole("MASTER_ADMIN") // Allow provisioning for master admins
        .requestMatchers("/api/master/tenants/**").hasRole("MASTER_ADMIN")
        .requestMatchers("/api/master/users/**").authenticated()
        .requestMatchers("/api/auth/login").permitAll()
        .requestMatchers("/api/users/**").authenticated()
        .requestMatchers("/api/locations/**").authenticated()
        .requestMatchers("/api/biometric-punch/**").permitAll() // Allow device punches
        // HRMS Module Endpoints
        .requestMatchers("/api/employees/**", "/api/departments/**", "/api/designations/**", "/api/job-details/**", "/api/jobBands/**", "/api/nationalities/**").authenticated()
        // Attendance & Leave Module Endpoints
        .requestMatchers("/api/attendance-records/**", "/api/time-attendence/**", "/api/time-types/**", "/api/work-types/**", "/api/shift-types/**", "/api/shift-policies/**", "/api/weekly-off-policies/**").authenticated()
        .requestMatchers("/api/leaves/**", "/api/leave-requests/**", "/api/leave-groups/**", "/api/leave-types/**", "/api/leave-policies/**", "/api/leave-allocations/**", "/api/leave-balances/**", "/api/leave-encashment-requests/**", "/api/leave-approvals/**").authenticated()
        // Payroll & Finance Module Endpoints
        .requestMatchers("/api/payrolls/**", "/api/payroll-runs/**", "/api/payslips/**", "/api/payroll-settings/**", "/api/salary-components/**", "/api/salary-structures/**", "/api/salary-structure-components/**", "/api/statutory-rules/**").authenticated()
        .requestMatchers("/api/loan-products/**", "/api/employee-loans/**", "/api/expenses/**", "/api/employee-bank-accounts/**").authenticated()
        // Company & Profile Endpoints
        .requestMatchers("/api/employee-documents/**", "/api/employee-profiles/**", "/api/company-info/**", "/api/company-locations/**", "/api/company-bank-accounts/**").authenticated()
        .requestMatchers("/api/pos/auth/login").permitAll()
        // Allow public viewing of uploaded files for the POS module
        .requestMatchers("/api/pos/uploads/view/**").permitAll()
        .requestMatchers("/public/products/**").permitAll() // Allow public access to product info via QR code
        .requestMatchers("/api/pos/**").authenticated()
        
        .anyRequest().denyAll()
      )
      .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
      .sessionManagement(sm -> sm.sessionCreationPolicy(
        org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "X-Tenant-ID"));
        config.setAllowCredentials(true);

        config.setExposedHeaders(Arrays.asList("Content-Disposition")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
