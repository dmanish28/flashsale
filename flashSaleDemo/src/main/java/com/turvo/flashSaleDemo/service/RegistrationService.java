package com.turvo.flashSaleDemo.service;

import java.util.List;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Registration;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;

public interface RegistrationService {

	 RegistrationOutput newRegistration(final Customer b, final FlashSale f);
	 
	 List<Registration> getRegistrationListByFlashSaleId(Integer id);
	 
	 Registration getRegistrationByFlashIdAndCustomerId (Integer fId,Integer cId);
}
