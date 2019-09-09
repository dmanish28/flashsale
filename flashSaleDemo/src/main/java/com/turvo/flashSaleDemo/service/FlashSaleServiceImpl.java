package com.turvo.flashSaleDemo.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turvo.flashSaleDemo.enums.OrderStatus;
import com.turvo.flashSaleDemo.enums.RegistrationStatus;
import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Order;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.model.Registration;
import com.turvo.flashSaleDemo.objects.PurchaseOutput;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;
import com.turvo.flashSaleDemo.repository.CustomerRepository;
import com.turvo.flashSaleDemo.repository.FlashSaleRepository;
import com.turvo.flashSaleDemo.repository.OrderRepository;
import com.turvo.flashSaleDemo.repository.ProductRepository;
import com.turvo.flashSaleDemo.repository.RegistrationRepository;
import com.turvo.flashSaleDemo.util.Constants;



@Service
public class FlashSaleServiceImpl implements FlashSaleService{

	@Autowired
	FlashSaleRepository flashSaleRepository;

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	CustomerService customerService;

	@Autowired
	CacheService<String ,Object> cacheService;

	@Autowired
	LockService lockService;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	RegistrationRepository registrationRepository;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private RegistrationService registrationService;
	
	@Autowired
	private EmailService emailService;

	private final ExecutorService executorService;

	public FlashSaleServiceImpl() {
		executorService = Executors.newFixedThreadPool(5);
	}

	@Override
	public FlashSale createFlashSale(Product product) throws Exception {
		// TODO Auto-generated method stub
		FlashSale flashSale = new FlashSale();
		flashSale.setStatus(Boolean.FALSE);
		flashSale.setProduct(product);
		flashSale.setRegistrationOpen(Boolean.TRUE);
		FlashSale returnedFS = flashSaleRepository.save(flashSale);
		
		//emailService.sendMail("", "", "");
		
		return returnedFS;
	}

	@Override
	public Boolean startFlashSale(FlashSale flashSale) {
		// TODO Auto-generated method stub
		cacheFlashSaleDetails(flashSale);
		flashSale.setRegistrationOpen(Boolean.FALSE);
		flashSale.setStatus(Boolean.TRUE);
		flashSaleRepository.saveAndFlush(flashSale);
		return true;
	}

	@Override
	public Boolean endFlashSale(FlashSale flashSale) {
		// TODO Auto-generated method stub
		flashSale.setStatus(Boolean.TRUE);
		flashSaleRepository.saveAndFlush(flashSale);

		return true;
	}

	@Override
	public RegistrationOutput register(Integer flashsaleId, Integer customerId) {
		Optional<FlashSale> flashSale = flashSaleRepository.findById(flashsaleId);
		Optional<Customer>  customer = customerRepository.findById(customerId);

		RegistrationOutput regOut = new RegistrationOutput();
		regOut.setStatus(Boolean.FALSE);

		if (flashSale == null || !flashSale.get().getRegistrationOpen() || flashSale.get().getStatus() == Boolean.TRUE) {
			regOut.setMessage("Invalid flashsale");//to externalize
		} else if (customer == null) {
			regOut.setMessage("Invalid customer");//to externalize
		} else {
			Registration registration = registrationService.getRegistrationByFlashIdAndCustomerId(flashSale.get().getId(),customer.get().getId());
			if (registration != null) {
				regOut.setMessage("customer already registered");
				regOut.setRegistrationId(registration.getId());
			} else {
				 regOut = registrationService.newRegistration(customer.get(),flashSale.get());
			}
		}
		return regOut;
	}


	@Override
	public PurchaseOutput purchase(final Integer flashsaleId, final Integer customerId){

		Integer productId =  (Integer)cacheService.getFromMemory(Constants.FLASHSALE_CACHE_PREFIX,flashsaleId.toString());
		final String productKey = Constants.PRODUCT_CACHE_PREFIX + "_" + flashsaleId + "_" + productId;
		final String customerKey = Constants.CUSTOMER_CACHE_PREFIX + "_" + flashsaleId + "_" + customerId;
		final List<String> watchKeys = Arrays.asList(productKey, customerKey);
		final Long end = System.currentTimeMillis() + Constants.BUY_TIMEOUT.longValue() * 1000 * 1000 * 1000;

		final PurchaseOutput purchaseOutput = new PurchaseOutput();
		purchaseOutput.setCustomerId(customerId);
		purchaseOutput.setProductId(productId);
		purchaseOutput.setStatus(Boolean.FALSE);

		Future<PurchaseOutput> future = executorService.submit(new Callable<PurchaseOutput>() {
			@Override
			public PurchaseOutput call() throws Exception {


				while (System.currentTimeMillis() < end) {
					final String readLock = lockService.acquireLockWithTimeout(Constants.ELIGIBILITY_LOCKNAME,
							Constants.LOCK_ACQUIRE_TIMEOUT, Constants.ELIGIBILITY_LOCK_TIMEOUT);
					if (readLock == null) {
						Thread.sleep(Constants.PURCHASE_CACHE_CYCLE_SLEEP);
						continue;
					}
					final String writeLock = lockService.acquireLockWithTimeout(Constants.BUY_LOCKNAME,
							Constants.LOCK_ACQUIRE_TIMEOUT, Constants.BUY_LOCK_TIMEOUT);

					if (writeLock == null) {
						lockService.releaseLock(Constants.ELIGIBILITY_LOCKNAME, readLock);
						Thread.sleep(Constants.PURCHASE_CACHE_CYCLE_SLEEP);
						continue;
					}

					//  transaction only if we have both locks
					return redisTemplate.execute(new SessionCallback<PurchaseOutput>() {
						@Override
						public PurchaseOutput execute(RedisOperations operations) throws DataAccessException {
							try {
								operations.watch(watchKeys);

								final Integer remainingUnits = (Integer) operations.opsForValue().get(productKey);
								final Boolean customerStatus = (Boolean) operations.opsForValue().get(customerKey);

								if (customerStatus != null && customerStatus && remainingUnits != null && remainingUnits > 0) {

									operations.multi();
									final Integer changedUnit = remainingUnits - 1;
									operations.opsForValue().set(productKey, changedUnit);
									operations.delete(customerKey);
									operations.exec();
									operations.unwatch();
									// purchased
									purchaseOutput.setStatus(Boolean.TRUE);

									persistPurchase(changedUnit, flashsaleId, customerId, productId);
									return purchaseOutput;
								} else {
									//cant buy
									operations.unwatch();
									return purchaseOutput;
								}
							} finally {
								lockService.releaseLock(Constants.ELIGIBILITY_LOCKNAME, readLock);
								lockService.releaseLock(Constants.BUY_LOCKNAME, writeLock);
							}
						}
					});
				}
				return purchaseOutput;

			}
		});

		try {
			return future.get(Constants.BUY_TIMEOUT.longValue(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.getMessage();
		} catch (ExecutionException e) {
			e.getMessage();
		} catch (TimeoutException e) {
			e.getMessage();
		}
		return purchaseOutput;
	}


	@Async
	@Transactional(readOnly = false)
	private Boolean persistPurchase(Integer newStockUnit, Integer flashSaleId, Integer customerId, Integer productId) {
		
		try{
			Optional<Product> product = productRepository.findById(productId);
		product.get().setStockUnit(newStockUnit);
		productRepository.saveAndFlush(product.get());

		Order order = new Order();
		order.setCustomer(customerRepository.findById(customerId).get());
		order.setProduct(productRepository.findById(productId).get());
		order.setCreatedAt(new Date());
		order.setOrderStatus(OrderStatus.APPROVED);
		orderRepository.saveAndFlush(order);

		Registration registration = registrationService.getRegistrationByFlashIdAndCustomerId(flashSaleId, customerId);
		registration.setRegistrationStatus(RegistrationStatus.PURCHASED);
		Registration savedRegistration = registrationRepository.saveAndFlush(registration);
		if(savedRegistration != null) {
			return false;
		}
		}catch(Exception e){
			return false;
		}
		return true;
		//orders can also be scheduled here
	}
	
	
	private void cacheFlashSaleDetails(FlashSale f) {
        List<Registration> registrationsForThisFlashsale = registrationService.getRegistrationListByFlashSaleId(f.getId());
        Product p = f.getProduct();

        // cache product id in memory, not going to change during sale
        cacheService.setInMemory( Constants.FLASHSALE_CACHE_PREFIX,f.getId().toString(), p.getId());

        // cache stock unit and customer information 
        cacheService.set( Constants.PRODUCT_CACHE_PREFIX,f.getId() + "_" + p.getId(), p.getStockUnit(),
                Constants.FLASHSALE_DURATION);
        for (Registration registration : registrationsForThisFlashsale) {
            if (registration.getRegistrationStatus() == RegistrationStatus.REGISTERED) {
                Customer customer = registration.getCustomer();
                cacheService.set( Constants.CUSTOMER_CACHE_PREFIX, f.getId() + "_" + customer.getId(), Boolean.TRUE, Constants.FLASHSALE_DURATION);
            }
        }
    }
}
