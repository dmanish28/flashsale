package com.turvo.flashSaleDemo.service;

import com.turvo.flashSaleDemo.exception.GlobalException;
import com.turvo.flashSaleDemo.model.FlashSale;
import com.turvo.flashSaleDemo.model.Product;
import com.turvo.flashSaleDemo.objects.PurchaseOutput;
import com.turvo.flashSaleDemo.objects.RegistrationOutput;

public interface FlashSaleService {
	
	FlashSale createFlashSale(Product product) throws GlobalException;
	
	FlashSale startFlashSale( FlashSale flashSale) throws GlobalException;

	FlashSale endFlashSale( FlashSale flashSale) throws GlobalException;

	RegistrationOutput register(final Integer flashsaleId, final Integer customerId) throws GlobalException;

	PurchaseOutput purchase(final Integer flashsaleId,final Integer customerId)throws GlobalException;
}
