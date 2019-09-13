package com.turvo.flashSaleDemo.service;

import com.turvo.flashSaleDemo.model.Customer;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;

public interface RegistrationService {

	 RegistrationOutput newRegistration(final Customer b, final FlashSale f);
}
