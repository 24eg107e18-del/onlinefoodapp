package com.example.foodordering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckoutRequest {

    @NotBlank(message = "Delivery address is required")
    @Size(min = 5, max = 250, message = "Delivery address must be between 5 and 250 characters")
    private String deliveryAddress;

    private String paymentMethod = "CASH_ON_DELIVERY";

    private String transactionId;

    public CheckoutRequest() {
    }

    public CheckoutRequest(String deliveryAddress, String paymentMethod) {
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
    }

    public CheckoutRequest(String deliveryAddress, String paymentMethod, String transactionId) {
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
        this.transactionId = transactionId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
