package com.turvo.flashSaleDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.turvo.flashSaleDemo.model.FlashSale;

@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {
}
