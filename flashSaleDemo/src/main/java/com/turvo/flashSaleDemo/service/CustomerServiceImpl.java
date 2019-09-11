package com.turvo.flashSaleDemo.service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.repository.CustomerRepository;


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
	public Customer createCustomer(Customer customer) {
		return customerRepository.saveAndFlush(customer);
	}

	@Override
	public List<Customer> getAllCustomers() {
		List<Customer> cutomers = customerRepository.findAll();	
		return cutomers;
	}

}
