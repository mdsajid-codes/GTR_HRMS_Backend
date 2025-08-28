package com.example.multi_tanent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.multi_tanent.security.JwtAuthFilter;
import com.example.multi_tanent.security.JwtUtil;

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean JwtUtil jwtUtil() { return new JwtUtil(); }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/master/auth/login").permitAll()
        .requestMatchers("/api/master/tenants/**").hasRole("MASTER_ADMIN")
        .requestMatchers("/api/auth/login").permitAll()
        .requestMatchers("/api/users").hasAnyRole("TENANT_ADMIN", "HR")
        .requestMatchers("/api/users/bulkUsers").hasAnyRole("TENANT_ADMIN", "HR")
        // Employee endpoints are secured at the method level with @PreAuthorize,
        // so we can set a general rule here.
        .requestMatchers("/api/employees/**").authenticated()
        
        .anyRequest().denyAll()
      )
      .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class)
      .sessionManagement(sm -> sm.sessionCreationPolicy(
        org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
    return http.build();
  }
}
