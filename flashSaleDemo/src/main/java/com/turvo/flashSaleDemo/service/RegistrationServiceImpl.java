package com.turvo.flashSaleDemo.service;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turvo.flashSaleDemo.enums.RegistrationStatus;
import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Registration;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;
import com.turvo.flashSaleDemo.repository.RegistrationRepository;


@Service
public class RegistrationServiceImpl implements RegistrationService{

	@Autowired
	RegistrationRepository registrationRepository;
	
	@Autowired
	EntityManager entityManager;
	
	
	@Override
    @Transactional(readOnly = false)
    public RegistrationOutput newRegistration( Customer b,  FlashSale f) {
        Registration registration = new Registration();
        registration.setCustomer(b);
        registration.setFlashSale(f);
        registration.setRegistrationStatus(RegistrationStatus.REGISTERED);
        registration = registrationRepository.saveAndFlush(registration);
        RegistrationOutput registrationResult = new RegistrationOutput();
        registrationResult.setMessage("SUCCESS!");
        registrationResult.setRegistrationId(registration.getId());
        registrationResult.setStatus(Boolean.TRUE);
        return registrationResult;
    }
}
