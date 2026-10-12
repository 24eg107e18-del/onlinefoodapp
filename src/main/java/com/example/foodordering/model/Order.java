package com.example.foodordering.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "orders")
public class Order {

    @Id
    private String id;

    @Indexed
    private String customerId;

    private String customerName;

    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

    private String deliveryAddress;

    private Instant orderDate = Instant.now();

    private String paymentMethod = "CASH_ON_DELIVERY";

    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Indexed
    private OrderStatus orderStatus = OrderStatus.PENDING;

    private String transactionId = "COD-PENDING";

    private Instant updatedAt = Instant.now();

    public Order() {
    }

    public Order(String id, String customerId, String customerName, List<OrderItem> items,
                 BigDecimal totalAmount, String deliveryAddress, String paymentMethod,
                 PaymentStatus paymentStatus, OrderStatus orderStatus) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = items != null ? items : new ArrayList<>();
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.orderDate = Instant.now();
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.orderStatus = orderStatus != null ? orderStatus : OrderStatus.PENDING;
        this.updatedAt = Instant.now();
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

    public List<OrderItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<OrderItem> items) {
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
