package com.turvo.flashSaleDemo.serviceImpl;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.service.CustomerService;
import com.turvo.flashSaleDemo.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService{
	
	private JavaMailSender javaMailSender;
	
	@Autowired
	private CustomerService customerService;

    @Autowired
    public EmailServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

 
    public void sendMail(String toEmail, String subject, String message) throws Exception {
     
        MimeMessage mm = javaMailSender.createMimeMessage();
        MimeMessageHelper mailMessage = new MimeMessageHelper(mm);
      
        mailMessage.setTo(getAllCustomerEmailIds());
        mailMessage.setSubject(subject);
        String html = "Flash sale is on <a href='http://localhost:8080/demo/v1/flashsale/1/5/register'>register</a> before June 30";
        mailMessage.setText(message,true);
       // mailMessage.setFrom("illuminatiwakes@gmail.com");
        javaMailSender.send(mm);
    }
    
    public InternetAddress[] getAllCustomerEmailIds() throws AddressException {
    	List<Customer> listOfCusts = customerService.getAllCustomersEmail();
    	InternetAddress[] emailConcatenated  = new InternetAddress[listOfCusts.size()];
    	// InternetAddress [] i = {new InternetAddress("illuminatiwakes@gmail.com"),new InternetAddress("manishwin4@gmail.com")};
    	for(int i =0 ;i< listOfCusts.size();i++ )
    	{    		
    		emailConcatenated[i] = new InternetAddress(listOfCusts.get(i).getEmailId());
    		
    	}
    	return emailConcatenated;
    }

}
