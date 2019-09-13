package com.turvo.flashSaleDemo;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import javax.mail.internet.InternetAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.objects.PurchaseOutput;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;
import com.turvo.flashSaleDemo.repository.CustomerRepository;
import com.turvo.flashSaleDemo.repository.FlashSaleRepository;
import com.turvo.flashSaleDemo.repository.OrderRepository;
import com.turvo.flashSaleDemo.repository.ProductRepository;
import com.turvo.flashSaleDemo.repository.RegistrationRepository;
import com.turvo.flashSaleDemo.service.CustomerService;
import com.turvo.flashSaleDemo.service.EmailService;
import com.turvo.flashSaleDemo.service.FlashSaleService;
import com.turvo.flashSaleDemo.service.LockService;




@RunWith(SpringRunner.class)
@SpringBootTest
public class FlashSaleDemoApplicationTests {

	@Autowired
	FlashSaleService flashSaleService;

	@Autowired
	CustomerService customerService;

	@Autowired
	EmailService emailService;

	@Autowired
	private LockService lockService;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private RegistrationRepository registrationRepository;

	@Autowired
	private FlashSaleRepository flashSaleRepository;


	private static final Integer NO_OF_CUSTOMERS= 15;

	private static final String RECIPIENT =  "abc@abc.com";

	private static final String MAIL_MESSAGE =  "FlashSale is on !! Register by JULY 30";

	private static final String SUBJECT =  "FLASH SALE";

	ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

	private FlashSale flashSale;
	private Product product;
	private List<Customer> listOfCusts = new ArrayList<>();
	@Before
	public void setUp() throws Exception {
		//for this project only , populating data for product
		product = new Product();
		product.setDescription("fossil watch");
		product.setStockUnit(2000);
		product.setName("watch");
		product.setPrice(12300);
		Product p = productRepository.saveAndFlush(product);

		for (Integer i = 1; i <= NO_OF_CUSTOMERS; i++) {
			Customer cust = new Customer();
			String customerName =  UUID.randomUUID().toString();
			cust.setName(customerName);
			cust.setEmailId(customerName+"@abc.com");
			listOfCusts.add(customerService.createCustomer(cust));
		}

		flashSale = flashSaleService.createFlashSale(p);
		System.out.print("Pre-processing done");
	}

	@After
	public void tearDown() {
		orderRepository.deleteAll();
		orderRepository.flush();
		registrationRepository.deleteAll();
		registrationRepository.flush();
		flashSaleRepository.deleteAll();
		flashSaleRepository.flush();
		customerRepository.deleteAll();
		customerRepository.flush();
		productRepository.deleteAll();
		productRepository.flush();
	}

	//just for populating customers
	@Test
	public void testCreateCustomer() {

		List<Customer> insertedCustomers = customerService.getAllCustomers();
		for (Customer cust:listOfCusts) {
			Assert.assertTrue(insertedCustomers.contains(cust));
		}
	}

	@Test
	public void testEmail()  {
		try {
			emailService.sendMail(InternetAddress.parse(RECIPIENT), SUBJECT,MAIL_MESSAGE);
			Assert.assertTrue("Mail sent!!", true);
		}catch(Exception me) {
			Assert.assertTrue("Failed to send mail. "+me.getMessage(), false);
		}
	}


	@Test
	public void testCreateFlashSale() throws Exception{

		Assert.assertEquals(product, flashSale.getProduct());
	}


	@Test
	public void testRegister() {
		RegistrationOutput regOutput = flashSaleService.register(flashSale.getId(), listOfCusts.get(0).getId());
		Assert.assertTrue("Registered!!", regOutput.getStatus());
	}


	@Test
	public void testRegisterMany() {
		List<Customer> listOfCustomers = customerService.getAllCustomers();
		for(Customer cust : listOfCustomers) {
			RegistrationOutput regOutput = flashSaleService.register(flashSale.getId(), cust.getId());
			Assert.assertTrue("Registered!!", regOutput.getStatus());
		}
	}

	@Test
	public void testRregisterAllAndStartFlashSaleAndPurchase() throws InterruptedException, ExecutionException, TimeoutException {

		List<Customer> listOfCustomers = customerService.getAllCustomers();
		for(Customer cust : listOfCustomers) {
			RegistrationOutput regOutput = flashSaleService.register(flashSale.getId(), cust.getId());
			Assert.assertTrue("Registered!!", regOutput.getStatus());
		}

		System.out.println("All registrations done!!");
		Assert.assertTrue("flashsale starts", flashSaleService.startFlashSale(flashSale));
		System.out.println("Flash Sale starts!!");
		List<Future<PurchaseOutput>> listOfPurchases = new ArrayList<>();

		for(Customer cust : listOfCustomers) {
			Future<PurchaseOutput> future = EXECUTOR_SERVICE.submit(new Callable<PurchaseOutput>() {

				@Override
				public PurchaseOutput call() throws Exception {
					// TODO Auto-generated method stub
					return flashSaleService.purchase(flashSale.getId(), cust.getId());
				}
			});
			
			listOfPurchases.add(future);
		}

		Set<Integer> successfulPurchase= new HashSet<>();
		Set<Integer> failedPurchase = new HashSet<>();

		for (Future<PurchaseOutput> puOutput : listOfPurchases) {

			if (puOutput != null && puOutput.get().getStatus()) {
				successfulPurchase.add(puOutput.get().getCustomerId());
			} else {
				failedPurchase.add(puOutput.get().getCustomerId());
			}
		}

		Optional<Product> p = productRepository.findById(flashSale.getProduct().getId());
		if(flashSale.getProduct().getStockUnit() > listOfCustomers.size()) {
			Integer remainingUnits = flashSale.getProduct().getStockUnit()-listOfCustomers.size();
			Assert.assertEquals("remaining stock",remainingUnits,p.get().getStockUnit());
		}
		else {
			Assert.assertEquals(flashSale.getProduct().getStockUnit(),(Integer)successfulPurchase.size());
			Assert.assertEquals(new Integer(0),p.get().getStockUnit());
		}	
	}

	@Test
	public void testG_endFlashSale() {

		Assert.assertTrue("flashsale ended",  flashSaleService.endFlashSale(flashSale));
	}

	@Test
	public void TestAcquireLock() {
		String lockIdent = lockService.acquireLockWithTimeout("lock", 10000L, 10000L);
		Assert.assertNotNull(lockIdent);
		String lockIdent1 = lockService.acquireLockWithTimeout("lock", 5000L, 10000L);
		Assert.assertNull(lockIdent1);
	}

	@Test
	public void TestReleaseLock() {
		String lock1 = lockService.acquireLockWithTimeout("lock", 10000L, 60000L);
		String lock2 = lockService.acquireLockWithTimeout("lock", 5000L, 60000L);
		Assert.assertNull(lock2);
		lockService.releaseLock("lock", lock1);
		lock2 = lockService.acquireLockWithTimeout("lock", 5000L, 60000L);
		Assert.assertNotNull(lock2);
		lockService.releaseLock("lock", lock2);
	}

}
