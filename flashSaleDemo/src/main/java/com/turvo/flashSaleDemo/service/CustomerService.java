package com.turvo.flashSaleDemo.service;

import java.util.List;

import com.turvo.flashSaleDemo.model.Customer;

public interface CustomerService {
		  
	  Customer createCustomer(String name);
	  
	  List<Customer> getAllCustomers() ;

}
