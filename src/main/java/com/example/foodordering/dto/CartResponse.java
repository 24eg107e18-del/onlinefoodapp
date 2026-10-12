package com.example.foodordering.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartResponse {

    private String customerId;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal deliveryFee = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalQuantity = 0;

    public CartResponse() {
    }

    public CartResponse(String customerId, List<CartItemResponse> items, BigDecimal subtotal,
                        BigDecimal deliveryFee, BigDecimal tax, BigDecimal totalAmount, int totalQuantity) {
        this.customerId = customerId;
        this.items = items != null ? items : new ArrayList<>();
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.totalQuantity = totalQuantity;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public List<CartItemResponse> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
