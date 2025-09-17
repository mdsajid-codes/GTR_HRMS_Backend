package com.example.multi_tanent.config;

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

@Configuration
@EnableMethodSecurity // enables @PreAuthorize
public class SecurityConfig {

  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwt) throws Exception {
    http.csrf(csrf -> csrf.disable())
      .cors(Customizer.withDefaults())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/master/auth/login").permitAll()
        .requestMatchers("/api/master/tenant-requests/register").permitAll()
        .requestMatchers("/api/master/tenant-requests/**").authenticated()
        .requestMatchers("/api/master/tenants/**").hasRole("MASTER_ADMIN")
        .requestMatchers("/api/auth/login").permitAll()
        .requestMatchers("/api/users/**").authenticated()
        .requestMatchers("/api/biometric-punch/**").permitAll() // Allow device punches
        .requestMatchers("/api/employee-documents/**").authenticated()
        .requestMatchers("/api/employee-profiles/**").authenticated()
        .requestMatchers("/api/employees/**").authenticated()
        .requestMatchers("/api/departments/**").authenticated()
        .requestMatchers("/api/time-attendence/**").authenticated()
        .requestMatchers("/api/time-types/**").authenticated()
        .requestMatchers("/api/work-types/**").authenticated()
        .requestMatchers("/api/shift-types/**").authenticated()
        .requestMatchers("/api/weekly-off-policies/**").authenticated()
        .requestMatchers("/api/job-details/**").authenticated()
        .requestMatchers("/api/designations/**").authenticated()
        .requestMatchers("/api/jobBands/**").authenticated()
        .requestMatchers("/api/shift-policies/**").authenticated()
        .requestMatchers("/api/attendance-records/**").authenticated()
        .requestMatchers("/api/nationalities/**").authenticated()
        .requestMatchers("/api/bankdetails/**").authenticated()
        .requestMatchers("/api/leaves/**").authenticated()
        .requestMatchers("/api/leave-requests/**").authenticated()
        .requestMatchers("/api/payrolls/**").authenticated()
        .requestMatchers("/api/payroll-setup/**").authenticated()
        .requestMatchers("/api/company-locations/**").authenticated()
        .requestMatchers("/api/company-bank-accounts/**").authenticated()
        .requestMatchers("/api/company-info/**").authenticated()
        .requestMatchers("/api/leave-groups/**").authenticated()
        .requestMatchers("/api/leave-types/**").authenticated()
        .requestMatchers("/api/leave-policies/**").authenticated()
        .requestMatchers("/api/leave-allocations/**").authenticated()
        .requestMatchers("/api/leave-balances/**").authenticated()
        .requestMatchers("/api/leave-encashment-requests/**").authenticated()
        .requestMatchers("/api/leave-approvals/**").authenticated()
        
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
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173", "https://gtrhrms.netlify.app","https://eclectic-arithmetic-e6d03e.netlify.app/")); // React app URL
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true); // If using cookies or auth headers

        config.setExposedHeaders(Arrays.asList("Content-Disposition")); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
