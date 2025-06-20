package com.example.TMDT_Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TmdtBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmdtBackendApplication.class, args);
	}

}
