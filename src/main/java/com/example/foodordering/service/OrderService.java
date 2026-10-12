package com.example.foodordering.service;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderItemResponse;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.exception.EmptyCartException;
import com.example.foodordering.exception.InvalidOrderException;
import com.example.foodordering.exception.InvalidStatusTransitionException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.*;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.OrderRepository;
import com.example.foodordering.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("40.00");
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("3.50");

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        FoodItemRepository foodItemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.foodItemRepository = foodItemRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse placeOrder(String customerId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(customerId);

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new EmptyCartException("Your cart is empty. Please add food items before checkout.");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        List<String> foodIds = cart.getItems().stream()
                .map(CartItem::getFoodItemId)
                .collect(Collectors.toList());

        Map<String, FoodItem> foodMap = foodItemRepository.findAllById(foodIds).stream()
                .collect(Collectors.toMap(FoodItem::getId, f -> f));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            FoodItem foodItem = foodMap.get(cartItem.getFoodItemId());

            if (foodItem == null) {
                throw new InvalidOrderException("One or more items in your cart no longer exist.");
            }

            if (!foodItem.isAvailable()) {
                throw new InvalidOrderException("Item '" + foodItem.getName() + "' is out of stock and cannot be ordered.");
            }

            // Snapshot food name and price at moment of ordering
            BigDecimal lineTotal = foodItem.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);

            orderItems.add(new OrderItem(
                    foodItem.getId(),
                    foodItem.getName(),
                    foodItem.getPrice(),
                    cartItem.getQuantity(),
                    lineTotal
            ));
        }

        // Server-calculated totals (never trusting frontend)
        BigDecimal deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : STANDARD_DELIVERY_FEE;
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(deliveryFee).add(tax).setScale(2, RoundingMode.HALF_UP);

        String deliveryAddress = request.getDeliveryAddress() != null && !request.getDeliveryAddress().isBlank()
                ? request.getDeliveryAddress().trim()
                : (customer.getAddress() != null ? customer.getAddress() : "Standard Delivery Address");

        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod().trim()
                : "CASH_ON_DELIVERY";

        boolean isOnlinePayment = "ONLINE_CARD".equalsIgnoreCase(paymentMethod)
                || "UPI".equalsIgnoreCase(paymentMethod)
                || "NET_BANKING".equalsIgnoreCase(paymentMethod);

        PaymentStatus initialPaymentStatus = isOnlinePayment ? PaymentStatus.PAID : PaymentStatus.PENDING;
        String transactionId = request.getTransactionId() != null && !request.getTransactionId().isBlank()
                ? request.getTransactionId().trim()
                : (isOnlinePayment ? "TXN-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase() : "COD-PENDING");

        Order order = new Order(
                null,
                customerId,
                customer.getName(),
                orderItems,
                totalAmount,
                deliveryAddress,
                paymentMethod,
                initialPaymentStatus,
                OrderStatus.PENDING
        );
        order.setTransactionId(transactionId);

        Order savedOrder = orderRepository.save(order);

        // Clear cart ONLY after successful order creation
        cartService.clearCart(customerId);

        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(String orderId, String currentUserId, boolean isAdmin) {
        Order order = findOrderById(orderId);

        // Security check: ensure customer can only view their own order
        if (!isAdmin && !order.getCustomerId().equals(currentUserId)) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        return mapToResponse(order);
    }

    public List<OrderResponse> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Page<OrderResponse> getCustomerOrders(String customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByOrderDateDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable)
                .map(this::mapToResponse);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);

        if (!order.getOrderStatus().canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition: Cannot change order status from " +
                            order.getOrderStatus() + " to " + newStatus + ". Valid next states: " +
                            order.getOrderStatus().getValidNextStates()
            );
        }

        order.setOrderStatus(newStatus);
        order.setUpdatedAt(Instant.now());

        // Update payment status for Cash on Delivery when delivered
        if (newStatus == OrderStatus.DELIVERED && "CASH_ON_DELIVERY".equalsIgnoreCase(order.getPaymentMethod())) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else if (newStatus == OrderStatus.CANCELLED) {
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    public Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getFoodItemId(),
                        i.getFoodName(),
                        i.getUnitPrice(),
                        i.getQuantity(),
                        i.getLineTotal()
                ))
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getCustomerName(),
                itemResponses,
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getOrderDate(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getOrderStatus(),
                order.getUpdatedAt()
        );
        response.setTransactionId(order.getTransactionId());
        return response;
    }
}
