package com.turvo.flashSaleDemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.turvo.flashSaleDemo.service.EmailService;

public class EmailServiceImpl implements EmailService{
	
	private JavaMailSender javaMailSender;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(String toEmail, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo("illuminatiwakes@");
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        mailMessage.setFrom("admin@admin.com");
        javaMailSender.send(mailMessage);
    }
    
    public String getAllCustomerEmailIds() {
    	return "";
    }

}
