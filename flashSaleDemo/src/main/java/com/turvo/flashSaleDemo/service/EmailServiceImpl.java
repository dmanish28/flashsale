package com.turvo.flashSaleDemo.service;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.model.Customer;

@Service
public class EmailServiceImpl implements EmailService{
	
	private JavaMailSender javaMailSender;
	
	@Autowired
	private CustomerService customerService;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

 
    public void sendMail(InternetAddress[] toEmail, String subject, String message) throws Exception {
     
        MimeMessage mm = javaMailSender.createMimeMessage();
        MimeMessageHelper mailMessage = new MimeMessageHelper(mm);
      
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message,true);
       
        javaMailSender.send(mm);
    }
    
    public InternetAddress[] getAllCustomerEmailIds() throws AddressException {
    	List<Customer> listOfCusts = customerService.getAllCustomers();
    	InternetAddress[] emailConcatenated  = new InternetAddress[listOfCusts.size()];
    	// InternetAddress [] i = {new InternetAddress("illuminatiwakes@gmail.com"),new InternetAddress("manishwin4@gmail.com")};
    	for(int i =0 ;i< listOfCusts.size();i++ )
    	{    		
    		emailConcatenated[i] = new InternetAddress(listOfCusts.get(i).getEmailId());
    		
    	}
    	return emailConcatenated;
    }

}
