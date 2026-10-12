package com.example.foodordering.dto;

import java.math.BigDecimal;
import java.util.Map;

public class AdminDashboardResponse {

    private long totalOrders;
    private long totalCustomers;
    private long totalFoodItems;
    private long totalCategories;
    private BigDecimal totalRevenue;
    private Map<String, Long> orderStatusCounts;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(long totalOrders, long totalCustomers, long totalFoodItems,
                                  long totalCategories, BigDecimal totalRevenue, Map<String, Long> orderStatusCounts) {
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalFoodItems = totalFoodItems;
        this.totalCategories = totalCategories;
        this.totalRevenue = totalRevenue;
        this.orderStatusCounts = orderStatusCounts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalFoodItems() {
        return totalFoodItems;
    }

    public void setTotalFoodItems(long totalFoodItems) {
        this.totalFoodItems = totalFoodItems;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Map<String, Long> getOrderStatusCounts() {
        return orderStatusCounts;
    }

    public void setOrderStatusCounts(Map<String, Long> orderStatusCounts) {
        this.orderStatusCounts = orderStatusCounts;
    }
}
