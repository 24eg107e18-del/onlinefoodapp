package com.example.foodordering.model;

public class CartItem {

    private String foodItemId;
    private int quantity;

    public CartItem() {
    }

    public CartItem(String foodItemId, int quantity) {
        this.foodItemId = foodItemId;
        this.quantity = quantity;
    }

    public String getFoodItemId() {
        return foodItemId;
    }

    public void setFoodItemId(String foodItemId) {
        this.foodItemId = foodItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
