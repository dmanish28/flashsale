package com.turvo.flashSaleDemo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.turvo.flashSaleDemo.model.Registration;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {

  
}
