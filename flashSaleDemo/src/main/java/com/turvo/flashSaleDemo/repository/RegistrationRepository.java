package com.turvo.flashSaleDemo.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.turvo.flashSaleDemo.enums.RegistrationStatus;
import com.turvo.flashSaleDemo.model.Registration;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

	Registration findByFlashSaleIdAndCustomerId(Integer flashId ,Integer CustomerId);
	
	List<Registration> findByFlashSaleIdAndRegistrationStatus(Integer flashId ,RegistrationStatus regStatus);

}
