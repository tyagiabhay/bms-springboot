package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BmsApplication {

	@GetMapping("/message")
	public String message(){
		return "Hello!!!";
	}

	public static void main(String[] args) {
		SpringApplication.run(BmsApplication.class, args);
	}

}
