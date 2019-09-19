package com.turvo.flashSaleDemo.objects;

public class PurchaseOutput {
	
	private Boolean status;

    private Integer customerId;

    private Integer productId;
    
    public PurchaseOutput() {
		super();
	}
    
	public PurchaseOutput(Boolean status, Integer customerId, Integer productId) {
		this.status = status;
		this.customerId = customerId;
		this.productId = productId;
	}

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public Integer getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}
}
