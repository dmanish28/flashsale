package com.turvo.flashSaleDemo.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.objects.PurchaseOutput;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;
import com.turvo.flashSaleDemo.service.CustomerService;
import com.turvo.flashSaleDemo.service.FlashSaleService;

@RestController
@RequestMapping(value = "/demo/v1/flashsale")
public class FlashSaleController {

	@Autowired
	FlashSaleService flashSaleService;
	@Autowired
	CustomerService customerService;
	
	@PostMapping("/{customerId}/{flashsaleId}/register")
	public ResponseEntity<RegistrationOutput> registerForFlashSale(
			@PathVariable Integer customerId, @PathVariable Integer flashsaleId) {

		RegistrationOutput regOut = flashSaleService.register(flashsaleId,customerId);

		if (regOut == null)
			return ResponseEntity.noContent().build();
		if(regOut.getMessage().equals("Invalid flashsale")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			        .body( regOut);
		}

		if(regOut.getMessage().equals("Invalid customer")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			        .body( regOut);
		}
		
		if(regOut.getMessage().equals("customer already registered")) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
			        .body( regOut);
		}
		
		return ResponseEntity.status(HttpStatus.CREATED).body(regOut);
	}
	
	@PostMapping("/{customerId}/{flashsaleId}/purchase")
	public ResponseEntity<PurchaseOutput> purchaseFlashSaleItem(
			@PathVariable Integer customerId, @PathVariable Integer flashsaleId) {

		PurchaseOutput purOut = flashSaleService.purchase(flashsaleId,customerId);

		if (purOut == null)
			return ResponseEntity.noContent().build();


		return ResponseEntity.status(HttpStatus.CREATED).body(purOut);
	}
	
	@PostMapping("/{customerId}/{flashsaleId}/purchaseMany")
	public ResponseEntity<PurchaseOutput> purchaseAllFlashSaleItem(
			@PathVariable Integer customerId, @PathVariable Integer flashsaleId) {

		List<Customer> listOfCustomers = customerService.getAllCustomers();
		PurchaseOutput purOut =null;
		for(Customer cust : listOfCustomers) {
			 purOut = flashSaleService.purchase(flashsaleId,cust.getId());

		}

		return ResponseEntity.status(HttpStatus.CREATED).body(purOut);
	}
	
	@PostMapping("/start")
	public ResponseEntity<Boolean> startFlashSale() {
		Product p = new Product();
		p.setDescription("abc");
			p.setId(1);
			p.setStockUnit(2000);
			p.setName("fossil");
			p.setPrice(12222);
			FlashSale f = new FlashSale();
			f.setId(6);
			f.setProduct(p);
			f.setRegistrationOpen(Boolean.FALSE);
			f.setStatus(Boolean.TRUE);
		boolean purOut = flashSaleService.startFlashSale(f);

		return ResponseEntity.status(HttpStatus.CREATED).body(purOut);
	}


}
