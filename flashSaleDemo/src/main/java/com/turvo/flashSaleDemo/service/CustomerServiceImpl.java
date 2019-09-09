package com.turvo.flashSaleDemo.service;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.repository.CustomerRepository;
import com.turvo.flashSaleDemo.util.Constants;


@Service
public class CustomerServiceImpl implements CustomerService{

	 private final ExecutorService executorService;

	    @Autowired
	    private LockService lockService;

	    @Autowired
	    private RedisTemplate<String, Object> redisTemplate;
	    
	    @Autowired
	    private CustomerRepository customerRepository;

	    public CustomerServiceImpl() {
			executorService = Executors.newFixedThreadPool(5);
		}


	 
	    
	    @Override
	    public Customer createCustomer(String name) {
	    	Customer c = new Customer();
	    	c.setName(name);
	    	c.setEmailId(name+"@abc.com");
	    	return customerRepository.saveAndFlush(c);
	    }
	    
	    @Override
	    public List<Customer> getAllCustomersEmail() {
	    	List<Customer> emails = customerRepository.findAll();	
	    	return emails;
	    }

}
