package com.turvo.flashSaleDemo.serviceImpl;

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
import com.turvo.flashSaleDemo.service.CustomerService;
import com.turvo.flashSaleDemo.service.LockService;
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
	    public Boolean checkIfEligible(final Integer flashsaleId, final Integer productId, final Integer customerId) {

	        final String productKey = Constants.PRODUCT_CACHE_PREFIX + "_" + flashsaleId + "_" + productId;
	        final String customerKey = Constants.CUSTOMER_CACHE_PREFIX + "_" + flashsaleId + "_" + customerId;

	        Future<Boolean> future = executorService.submit(new Callable<Boolean>() {
	            @Override
	            public Boolean call() throws Exception {
	                return redisTemplate.execute(new SessionCallback<Boolean>() {
	                    @Override
	                    public Boolean execute(RedisOperations operations) throws DataAccessException {
	                        String lock = null;
	                        try {
	                            lock = lockService.acquireLockWithTimeout(Constants.ELIGIBILITY_LOCKNAME,
	                                    Constants.LOCK_ACQUIRE_TIMEOUT, Constants.ELIGIBILITY_LOCK_TIMEOUT);
	                            if (lock == null) {
	                                return Boolean.FALSE;
	                            }
	                            final Integer remainingStock = (Integer) operations.opsForValue().get(productKey);
	                            final Boolean customerStatus = (Boolean) operations.opsForValue().get(customerKey);
	                            if (customerStatus != null && customerStatus && remainingStock != null && remainingStock > 0) {
	                                return Boolean.TRUE;
	                            }
	                            return Boolean.FALSE;
	                        } finally {
	                            if (lock != null)
	                                lockService.releaseLock(Constants.ELIGIBILITY_LOCKNAME, lock);
	                        }
	                    }
	                });
	            }
	        });
	        try {
	            return future.get(Constants.CUSTOMER_ELIGIBILITY_QUERY_TIMEOUT.longValue(), TimeUnit.SECONDS);
	        } catch (InterruptedException e) {
	           
	        } catch (ExecutionException e) {
	           
	        } catch (TimeoutException e) {
	           
	        }
	        return Boolean.FALSE;
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
