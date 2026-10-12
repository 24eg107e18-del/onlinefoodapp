package com.example.foodordering.dto;

import java.math.BigDecimal;

public class OrderItemResponse {

    private String foodItemId;
    private String foodName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal lineTotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(String foodItemId, String foodName, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
        this.foodItemId = foodItemId;
        this.foodName = foodName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public String getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(String foodItemId) {
        this.foodItemId = foodItemId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
}
