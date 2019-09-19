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

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turvo.flashSaleDemo.enums.OrderStatus;
import com.turvo.flashSaleDemo.enums.RegistrationStatus;
import com.turvo.flashSaleDemo.exception.GlobalException;
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
	public FlashSale createFlashSale(Product product) throws GlobalException {
		// TODO Auto-generated method stub
		try {
			FlashSale flashSale = new FlashSale();
			flashSale.setStatus(Boolean.FALSE);
			flashSale.setProduct(product);
			flashSale.setRegistrationOpen(Boolean.TRUE);

			String subject = "Flash Sale!!";
			String message = "Flash sale is on!! Register before June 30";
			try {
				emailService.sendMail(emailService.getAllCustomerEmailIds(),subject ,message);
			}catch (MailException | MessagingException  e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			return flashSaleRepository.save(flashSale);
		} catch ( DataAccessException e) {
			// TODO Auto-generated catch block
			throw new GlobalException("Failed to create flash sale",e);
		}
	}

	@Override
	public FlashSale startFlashSale(FlashSale flashSale) throws GlobalException{
		// TODO Auto-generated method stub
		try {
			cacheFlashSaleDetails(flashSale);
			flashSale.setRegistrationOpen(Boolean.FALSE);
			flashSale.setStatus(Boolean.TRUE);
			return flashSaleRepository.saveAndFlush(flashSale);
		}catch (DataAccessException e) {
			// TODO Auto-generated catch block
			throw new GlobalException("Failed to start flash sale",e);
		}
	}

	@Override
	public FlashSale endFlashSale(FlashSale flashSale) throws GlobalException{
		// TODO Auto-generated method stub
		try {
			flashSale.setStatus(Boolean.TRUE);
			return flashSaleRepository.saveAndFlush(flashSale);
		}catch ( DataAccessException e) {
			// TODO Auto-generated catch block
			throw new GlobalException("Failed to end flash sale",e);
		}
	}

	@Override
	public RegistrationOutput register(Integer flashsaleId, Integer customerId) throws GlobalException {
		try {
			Optional<FlashSale> flashSale = flashSaleRepository.findById(flashsaleId);
			Optional<Customer>  customer = customerRepository.findById(customerId);

			RegistrationOutput regOut = new RegistrationOutput();
			regOut.setStatus(Boolean.FALSE);

			if (flashSale == null || !flashSale.get().getRegistrationOpen() || flashSale.get().getStatus() == Boolean.TRUE) {
				regOut.setMessage("Invalid registration request");//to externalize
			} else if (customer == null) {
				regOut.setMessage("Invalid customer request");//to externalize
			} else {
				Registration registration = registrationRepository.findByFlashSaleIdAndCustomerId(flashSale.get().getId(),customer.get().getId());
				if (registration != null) {
					regOut.setMessage("customer already registered");
					regOut.setRegistrationId(registration.getId());
				} else {
					regOut = registrationService.newRegistration(customer.get(),flashSale.get());
				}
			}
			return regOut;
		}catch (DataAccessException e) {
			// TODO Auto-generated catch block
			throw new GlobalException("Failed to register in flash sale",e);
		}
	}


	@Override
	public PurchaseOutput purchase(final Integer flashsaleId, final Integer customerId) throws GlobalException{

		try {
			Integer productId =  (Integer)cacheService.getFromMemory(Constants.FLASHSALE_CACHE_PREFIX,flashsaleId.toString());
			final String productKey = Constants.PRODUCT_CACHE_PREFIX + "_" + flashsaleId + "_" + productId;
			final String customerKey = Constants.CUSTOMER_CACHE_PREFIX + "_" + flashsaleId + "_" + customerId;
			final List<String> watchKeys = Arrays.asList(productKey, customerKey);
			final Long end = System.currentTimeMillis() + Constants.BUY_TIMEOUT.longValue() * 1000  * 1000 * 1000;

			final PurchaseOutput purchaseOutput = new PurchaseOutput(Boolean.FALSE, customerId, productId);

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
										// System.out.println("unit: " + remainingUnits + ", customer: " + customerId);

										operations.multi();

										final Integer changedUnit =  remainingUnits - 1;
										operations.opsForValue().set(productKey, changedUnit);
										operations.delete(customerKey);
										operations.exec();
										operations.unwatch();
										// System.out.println("Purchase successful, customer: " + customerId);								
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
			return future.get(Constants.BUY_TIMEOUT.longValue(), TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			throw new GlobalException("Failed to purchase the product",e);
		}
	}


	@Async
	@Transactional(readOnly = false)
	private void persistPurchase(Integer newStockUnit, Integer flashSaleId, Integer customerId, Integer productId) {
		
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

			Registration registration = registrationRepository.findByFlashSaleIdAndCustomerId(flashSaleId, customerId);
			registration.setRegistrationStatus(RegistrationStatus.PURCHASED);
			registrationRepository.saveAndFlush(registration);
			//orders can also be scheduled here
			
		}
		catch(Exception e){
			e.printStackTrace();
			if(recacheFlashSaleDetails(flashSaleId,  customerId,  productId) == Boolean.FALSE) {
				System.out.println("Failed to update cache on failure");
			}
		}
	}
	
	private Object recacheFlashSaleDetails(Integer flashSaleId, Integer customerId, Integer productId) {
		
		final String productKey = Constants.PRODUCT_CACHE_PREFIX + "_" + flashSaleId + "_" + productId;
		final String customerKey = Constants.CUSTOMER_CACHE_PREFIX + "_" + flashSaleId + "_" + customerId;
		final List<String> watchKeys = Arrays.asList(productKey, customerKey,Constants.RECACHE);
		final Long end = System.currentTimeMillis() + Constants.BUY_TIMEOUT.longValue() * 1000  * 1000 * 1000;

		while (System.currentTimeMillis() < end) {
			final String readLock = lockService.acquireLockWithTimeout(Constants.RECACHE_LOCKNAME,
					Constants.LOCK_ACQUIRE_TIMEOUT, Constants.RECAHE_LOCK_TIMEOUT);
			if (readLock == null) {
				try {
					Thread.sleep(Constants.PURCHASE_CACHE_CYCLE_SLEEP);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			final String writeLock = lockService.acquireLockWithTimeout(Constants.RECACHE_WRITE_LOCKNAME,
					Constants.LOCK_ACQUIRE_TIMEOUT, Constants.RECAHE_WRITE_LOCK_TIMEOUT);

			if (writeLock == null) {
				lockService.releaseLock(Constants.RECACHE_WRITE_LOCKNAME, readLock);
				try {
					Thread.sleep(Constants.PURCHASE_CACHE_CYCLE_SLEEP);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			return redisTemplate.execute(new SessionCallback() {
				@Override
				public Object execute(RedisOperations operations) throws DataAccessException {
					// TODO Auto-generated method stub

					try {
						final Integer remainingUnits = (Integer) operations.opsForValue().get(productKey);
						operations.watch(watchKeys);
						operations.multi();
						final Integer changedUnit =  remainingUnits + 1;
						operations.opsForValue().set(productKey, changedUnit);
						operations.opsForValue().set(customerKey, Boolean.TRUE);
						operations.exec();
						operations.unwatch();
						return Boolean.TRUE;
					} finally {
						lockService.releaseLock(Constants.RECACHE_LOCKNAME, readLock);
						lockService.releaseLock(Constants.RECACHE_WRITE_LOCKNAME, writeLock);
					}
				}

			});
		}
		return Boolean.FALSE;
	}
	
	private void cacheFlashSaleDetails(FlashSale f) {
        List<Registration> registrationsForThisFlashsale = registrationRepository.findByFlashSaleIdAndRegistrationStatus(f.getId(),RegistrationStatus.REGISTERED);
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
