package com.example.zubzub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class ZubzubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZubzubApplication.class, args);
	}

}
