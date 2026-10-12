package com.example.foodordering.dto;

import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderResponse {

    private String id;
    private String customerId;
    private String customerName;
    private List<OrderItemResponse> items = new ArrayList<>();
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private Instant orderDate;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private Instant updatedAt;

    public OrderResponse() {
    }

    public OrderResponse(String id, String customerId, String customerName, List<OrderItemResponse> items,
                         BigDecimal totalAmount, String deliveryAddress, Instant orderDate,
                         String paymentMethod, PaymentStatus paymentStatus, OrderStatus orderStatus, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.orderDate = orderDate;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<OrderItemResponse> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Instant getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Instant orderDate) {
        this.orderDate = orderDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
