package com.turvo.flashSaleDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turvo.flashSaleDemo.exception.GlobalException;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.service.FlashSaleService;

@RestController
@RequestMapping(value = "/demo/v1/flashsale")
public class FlashSaleControllerInternal {
	
	@Autowired
	FlashSaleService flashSaleService;

	@PostMapping("/start")
	public ResponseEntity<Object> startFlashSale(@RequestBody FlashSale flashSale) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(flashSaleService.startFlashSale(flashSale));
		}catch(GlobalException ge) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ge.getMessage());
		}	
	}

	@PostMapping("/end")
	public ResponseEntity<Object> endFlashSale(@RequestBody FlashSale flashSale) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(flashSaleService.endFlashSale(flashSale));
		}catch(GlobalException ge) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ge.getMessage());
		}	
	}

	@PostMapping("/create")
	public ResponseEntity<Object> createFlashSale(@RequestBody Product product) {
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(flashSaleService.createFlashSale(product));
		}catch(GlobalException ge) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ge.getMessage());
		}	
	}

}
