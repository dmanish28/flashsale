package com.turvo.flashSaleDemo.service;

import java.util.List;

import com.turvo.flashSaleDemo.model.Customer;

public interface CustomerService {
	
	  Boolean checkIfEligible(final Integer flashsaleId, final Integer productId, final Integer customerId);
	  
	  Customer createCustomer(String name);
	  
	  List<Customer> getAllCustomersEmail() ;

}
