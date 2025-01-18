package com.example.wildtide;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
	public static void main(String[] args) {
		Lockey_Manager.initStartup();
		SpringApplication.run(SpringApplication.class, args);
	}
}
