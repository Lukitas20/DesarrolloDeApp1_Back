package com.example.TpDA1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TpDa1Application {

	public static void main(String[] args) {
		SpringApplication.run(TpDa1Application.class, args);
	}

}