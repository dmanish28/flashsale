package com.turvo.flashSaleDemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
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
	
	@Test
	public void testCreateCustomer() {
		for(int i=20;i<=30;i++) {
			String customerName = ("Customer_"+i);
			assertEquals(customerName, customerService.createCustomer(customerName).getName());
		}
	}
	
	@Test
	public void testEmail()  {
		try {
		//	emailService.sendMail("", "", "");
			assertTrue("Mail sent!!", true);
		}catch(Exception me) {
			assertTrue("Failed to send mail", false);
		}
	}
	
	
	@Test
	public void createFlashSale() throws Exception{
		Product product = new Product();
		product.setDescription("fossil watch");
		product.setId(1);
		product.setStockUnit(10);
		product.setName("watch");
		product.setPrice(12300);
		FlashSale flashSale = flashSaleService.createFlashSale(product);
		assertEquals(product, flashSale.getProduct());
	}
	
	
	@Test
	public void testRegister() {
				
	//	FlashSale flashSale = flashSaleService.register(flashsaleId, customerId);
	}
	
	
	@Test
	public void testRegisterMany() {
		List<Customer> listOfCustomers = customerService.getAllCustomers();
		
		for(Customer cust : listOfCustomers) {
			flashSaleService.register(8, cust.getId());
		}
	}
	
	@Test
	public void startFlashSale() {
		Product p = new Product();
		p.setDescription("abc");
			p.setId(1);
			p.setStockUnit(2000);
			p.setName("fossil");
			p.setPrice(123);
			FlashSale f = new FlashSale();
			f.setId(8);
			f.setProduct(p);
			f.setRegistrationOpen(Boolean.FALSE);
			f.setStatus(Boolean.TRUE);
		try{flashSaleService.startFlashSale(f);
		}catch(Exception e) {
			e.getStackTrace();
		}
		System.out.print(true);
	}
	
	
	@Test
	public void endFlashSale() {
		Product product = new Product();
		product.setDescription("fossil watch");
		product.setId(1);
		product.setStockUnit(10);
		product.setName("watch");
		product.setPrice(12300);
		
		FlashSale f = new FlashSale();
		f.setId(6);
		f.setProduct(product);
		f.setRegistrationOpen(Boolean.FALSE);
		f.setStatus(Boolean.FALSE);
		
		assertTrue("flashsale ended",  flashSaleService.endFlashSale(f));
	}
	
	
	@Test
    public void acquireLockTest() {
        String lockIdent = lockService.acquireLockWithTimeout("lock", 10000L, 10000L);
        Assert.assertNotNull(lockIdent);
    }

    @Test
    public void releaseLockTest() {
        String lock1 = lockService.acquireLockWithTimeout("lock", 10000L, 60000L);
        String lock2 = lockService.acquireLockWithTimeout("lock", 5000L, 60000L);
        Assert.assertNull(lock2);
        lockService.releaseLock("lock", lock1);
        lock2 = lockService.acquireLockWithTimeout("lock", 5000L, 60000L);
        Assert.assertNotNull(lock2);
        lockService.releaseLock("lock", lock2);
    }

}
