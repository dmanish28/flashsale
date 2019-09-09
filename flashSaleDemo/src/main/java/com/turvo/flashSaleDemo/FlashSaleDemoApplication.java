package com.turvo.flashSaleDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class FlashSaleDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashSaleDemoApplication.class, args);
	}

}
