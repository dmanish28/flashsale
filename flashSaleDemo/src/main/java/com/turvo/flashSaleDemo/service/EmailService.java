package com.turvo.flashSaleDemo.service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public interface EmailService {
	
	void sendMail(String toEmail, String subject, String message) throws Exception; 
	InternetAddress[] getAllCustomerEmailIds()  throws AddressException ;
}
