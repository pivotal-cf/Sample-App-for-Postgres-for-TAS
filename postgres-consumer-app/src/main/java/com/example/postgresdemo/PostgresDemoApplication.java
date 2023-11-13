package com.example.postgresdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Map;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan(basePackages = {"com.example.postgresdemo"})
public class PostgresDemoApplication {
	public static void main(String[] args) {
		System.out.println("-----------ENV START--------------------------");
		Map<String, String> envMap = System.getenv();

		for (String envName : envMap.keySet()) {
			System.out.format("%s = %s%n", envName, envMap.get(envName));
		}
		System.out.println("------------ENV END-------------------------");

		SpringApplication.run(PostgresDemoApplication.class, args);
	}
}
