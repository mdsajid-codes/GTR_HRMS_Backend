package com.example.multi_tanent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MultiTanentApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MultiTanentApplication.class, args);
	}

}
