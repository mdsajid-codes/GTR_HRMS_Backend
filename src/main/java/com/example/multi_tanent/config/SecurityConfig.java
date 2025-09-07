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
        .requestMatchers("/api/employees/**").authenticated()
        .requestMatchers("/api/departments/**").authenticated()
        .requestMatchers("/api/designations/**").authenticated()
        .requestMatchers("/api/jobDetails/**").authenticated()
        .requestMatchers("/api/jobFillings/**").authenticated()
        .requestMatchers("/api/salaryDetails/**").authenticated()
        .requestMatchers("/api/compensations/**").authenticated()
        .requestMatchers("/api/bankdetails/**").authenticated()
        .requestMatchers("/api/leaves/**").authenticated()
        .requestMatchers("/api/payrolls/**").authenticated()
        
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
        config.setAllowedOrigins(java.util.List.of("http://localhost:5173")); // React app URL
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setAllowCredentials(true); // If using cookies or auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
