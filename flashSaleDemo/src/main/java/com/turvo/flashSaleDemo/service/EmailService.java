package com.turvo.flashSaleDemo.service;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.springframework.mail.MailException;

public interface EmailService {
	
	void sendMail(InternetAddress[] toEmail, String subject, String message) throws MailException,MessagingException; 
	
	InternetAddress[] getAllCustomerEmailIds()  throws AddressException ;
}
