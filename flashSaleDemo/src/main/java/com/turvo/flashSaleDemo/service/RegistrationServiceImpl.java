package com.turvo.flashSaleDemo.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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


	
	public List<Registration> getRegistrationListByFlashSaleId (Integer flashId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Registration> criteriaQuery = criteriaBuilder.createQuery(Registration.class);
		Root<Registration> itemRoot = criteriaQuery.from(Registration.class);
		
		Predicate predicateForFlash
		  = criteriaBuilder.equal(itemRoot.get("flashSale"),flashId );
		Predicate predicateForRegStatus
		  = criteriaBuilder.equal(itemRoot.get("registrationStatus"), RegistrationStatus.REGISTERED);
		Predicate predicateForFlashAndRegStatus
		  = criteriaBuilder.and(predicateForFlash, predicateForRegStatus);
		 
				 
		criteriaQuery.where(predicateForFlashAndRegStatus);
		List<Registration> items = entityManager.createQuery(criteriaQuery).getResultList();
		
		return items;
	}
	
	public Registration getRegistrationByFlashIdAndCustomerId (Integer fId,Integer cId) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Registration> criteriaQuery = criteriaBuilder.createQuery(Registration.class);
		Root<Registration> itemRoot = criteriaQuery.from(Registration.class);
		
		Predicate predicateForFlash
		  = criteriaBuilder.equal(itemRoot.get("flashSale"),fId );
		Predicate predicateForCustomer
		  = criteriaBuilder.equal(itemRoot.get("customer"),cId);
		Predicate predicateForFlashAndCustomer
		  = criteriaBuilder.and(predicateForFlash, predicateForCustomer);
		 
				 
		criteriaQuery.where(predicateForFlashAndCustomer);
		try {
		Registration reg = entityManager.createQuery(criteriaQuery).getSingleResult();
		return reg;
		}catch(Exception e) {
			return null;
		}
		
	}

}
