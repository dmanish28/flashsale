package com.turvo.flashSaleDemo.model;


import javax.persistence.*;
import com.turvo.flashSaleDemo.enums.*;

@Entity
@Table(name = "registration")
public class Registration {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "serial")
    private Integer id;

    @OneToOne()
    @JoinColumn(name = "flashSaleId")
    private FlashSale flashSale;

    @OneToOne()
    @JoinColumn(name = "customerId")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status")
    private RegistrationStatus registrationStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public FlashSale getFlashSale() {
		return flashSale;
	}

	public void setFlashSale(FlashSale flashSale) {
		this.flashSale = flashSale;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public RegistrationStatus getRegistrationStatus() {
		return registrationStatus;
	}

	public void setRegistrationStatus(RegistrationStatus registrationStatus) {
		this.registrationStatus = registrationStatus;
	}
    
    
    
    
}
