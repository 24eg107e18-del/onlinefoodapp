package com.example.foodordering.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private String foodItemId;
    private String foodName;
    private BigDecimal price;
    private String imageUrl;
    private int quantity;
    private BigDecimal lineTotal;
    private boolean available;

    public CartItemResponse() {
    }

    public CartItemResponse(String foodItemId, String foodName, BigDecimal price, String imageUrl,
                            int quantity, BigDecimal lineTotal, boolean available) {
        this.foodItemId = foodItemId;
        this.foodName = foodName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.available = available;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
