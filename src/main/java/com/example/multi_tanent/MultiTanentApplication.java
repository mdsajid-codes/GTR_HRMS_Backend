package com.example.multi_tanent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.servlet.MultipartConfigElement;
import org.springframework.util.unit.DataSize;

@SpringBootApplication
@EnableScheduling
public class MultiTanentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiTanentApplication.class, args);
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(DataSize.ofMegabytes(10)); // Set max file size to 10MB
		factory.setMaxRequestSize(DataSize.ofMegabytes(10)); // Set max request size to 10MB
		return factory.createMultipartConfig();
	}
}
