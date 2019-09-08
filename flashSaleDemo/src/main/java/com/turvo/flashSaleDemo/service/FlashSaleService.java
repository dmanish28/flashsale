package com.turvo.flashSaleDemo.service;

import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.objects.PurchaseOutput;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;

public interface FlashSaleService {
	
	FlashSale createFlashSale(Product product) throws Exception;
	
	Boolean startFlashSale( FlashSale flashSale);

	Boolean endFlashSale( FlashSale flashSale);

	RegistrationOutput register(final Integer flashsaleId, final Integer customerId);

	Boolean isEligible(final Integer flashsaleId, final Integer customerId);

	PurchaseOutput purchase(final Integer flashsaleId,final Integer customerId);
}
