package com.example.exaple06;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // ✅ Thêm import này

@SpringBootApplication
@EnableScheduling // ✅ Thêm annotation này
public class Exaple06Application {

	public static void main(String[] args) {
		SpringApplication.run(Exaple06Application.class, args);
	}

}
