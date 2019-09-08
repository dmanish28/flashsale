package com.turvo.flashSaleDemo;

import static org.junit.Assert.assertEquals;

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

import junit.framework.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FlashSaleDemoApplicationTests {

	@Autowired
	FlashSaleService flashSaleService;
	@Autowired
	CustomerService customerService;
	
	@Autowired
	EmailService emailService;
	
	@Test
	public void testCreateCustomer() {
		for(int i=2;i<=10;i++) {
			String customerName = ("Customer_"+i);
			assertEquals(customerName, customerService.createCustomer(customerName).getName());
		}
	}
	
	@Test
	public void testEmail() throws Exception {
		
	emailService.sendMail("", "", "");
		System.out.print(true);
		
	}
	
	@Test
	public void startFlashSale() {
		Product p = new Product();
		p.setDescription("asd");
			p.setId(1);
			p.setStockUnit(1);
			p.setName("ad");
			p.setPrice(123);
			FlashSale f = new FlashSale();
			f.setId(3);
			f.setProduct(p);
			f.setRegistrationOpen(Boolean.FALSE);
			f.setStatus(Boolean.TRUE);
		flashSaleService.startFlashSale(f);
		System.out.print(true);
	}
	
	@Test
	public void createFlashSale() throws Exception {
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
		Product product = new Product();
		product.setDescription("fossil watch");
		product.setId(1);
		product.setStockUnit(10);
		product.setName("watch");
		product.setPrice(12300);
	//	FlashSale flashSale = flashSaleService.register(flashsaleId, customerId);
	//(product, flashSale.getProduct());
	}
	
	
	
	@Test
	public void endFlashSale() {
		Product product = new Product();
		product.setDescription("fossil watch");
		product.setId(1);
		product.setStockUnit(10);
		product.setName("watch");
		product.setPrice(12300);
	//	FlashSale flashSale = flashSaleService.endFlashSale(flashSale)
	//	assertEquals(product, flashSale.getProduct());
	}

}
