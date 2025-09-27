package com.example.multi_tanent.master.controller;

import com.example.multi_tanent.master.entity.MasterUser;
import com.example.multi_tanent.master.repository.MasterUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class MasterDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(MasterDataInitializer.class);

    // You can override these values in your application.yml or application.properties
    @Value("${app.default-master-admin.username:masteradmin}")
    private String defaultUsername;

    @Value("${app.default-master-admin.password:password123}")
    private String defaultPassword;

    @Bean
    public ApplicationRunner initializeMasterAdmin(MasterUserRepository masterUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (masterUserRepository.findByUsername(defaultUsername).isEmpty()) {
                logger.info("Default master admin user not found. Creating user '{}'.", defaultUsername);
                MasterUser masterUser = new MasterUser();
                masterUser.setUsername(defaultUsername);
                masterUser.setPasswordHash(passwordEncoder.encode(defaultPassword));
                masterUserRepository.save(masterUser);
                logger.info("============================================================");
                logger.info("Default Master Admin created. Username: {}, Password: {}", defaultUsername, defaultPassword);
                logger.info("============================================================");
            }
        };
    }
}