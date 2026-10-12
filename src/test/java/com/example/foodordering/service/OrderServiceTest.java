package com.example.foodordering.service;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.exception.EmptyCartException;
import com.example.foodordering.exception.InvalidOrderException;
import com.example.foodordering.exception.InvalidStatusTransitionException;
import com.example.foodordering.model.*;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.OrderRepository;
import com.example.foodordering.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private FoodItem foodItem1;
    private Cart activeCart;

    @BeforeEach
    void setUp() {
        testUser = new User("user-1", "Alice Smith", "alice@test.com", "hash", "1112223333", "456 Oak Lane", Role.CUSTOMER);
        foodItem1 = new FoodItem("food-1", "Chicken Burger", "Juicy burger", "cat-1", new BigDecimal("15.00"), "http://img.com/burger.jpg", true);
        activeCart = new Cart("user-1");
    }

    @Test
    @DisplayName("Should reject order placement when cart is empty")
    void testPlaceOrderEmptyCartThrowsException() {
        when(cartService.getOrCreateCart("user-1")).thenReturn(activeCart);

        CheckoutRequest request = new CheckoutRequest("456 Oak Lane", "CASH_ON_DELIVERY");

        assertThrows(EmptyCartException.class, () -> orderService.placeOrder("user-1", request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should successfully place order with server-calculated total and clear cart")
    void testPlaceOrderSuccess() {
        activeCart.getItems().add(new CartItem("food-1", 2)); // 2 * 15.00 = 30.00 subtotal

        when(cartService.getOrCreateCart("user-1")).thenReturn(activeCart);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(foodItemRepository.findAllById(List.of("food-1"))).thenReturn(List.of(foodItem1));

        Order savedOrder = new Order(
                "order-101",
                "user-1",
                "Alice Smith",
                List.of(new OrderItem("food-1", "Chicken Burger", new BigDecimal("15.00"), 2, new BigDecimal("30.00"))),
                new BigDecimal("35.00"), // subtotal 30 + delivery 3.50 + tax 1.50 = 35.00
                "456 Oak Lane",
                "CASH_ON_DELIVERY",
                PaymentStatus.PENDING,
                OrderStatus.PENDING
        );
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        CheckoutRequest request = new CheckoutRequest("456 Oak Lane", "CASH_ON_DELIVERY");
        OrderResponse response = orderService.placeOrder("user-1", request);

        assertNotNull(response);
        assertEquals("order-101", response.getId());
        assertEquals("user-1", response.getCustomerId());
        assertEquals(OrderStatus.PENDING, response.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());

        // Verify cart is cleared after order creation
        verify(cartService, times(1)).clearCart("user-1");
    }

    @Test
    @DisplayName("Should reject order placement if an item becomes unavailable")
    void testPlaceOrderUnavailableItem() {
        activeCart.getItems().add(new CartItem("food-1", 1));
        foodItem1.setAvailable(false); // item out of stock

        when(cartService.getOrCreateCart("user-1")).thenReturn(activeCart);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(foodItemRepository.findAllById(List.of("food-1"))).thenReturn(List.of(foodItem1));

        CheckoutRequest request = new CheckoutRequest("456 Oak Lane", "CASH_ON_DELIVERY");

        assertThrows(InvalidOrderException.class, () -> orderService.placeOrder("user-1", request));
        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart(anyString());
    }

    @Test
    @DisplayName("Should prevent customers from viewing another customer's order")
    void testCustomerOwnershipCheck() {
        Order otherCustomerOrder = new Order(
                "order-999",
                "user-2", // different customer
                "Bob",
                new ArrayList<>(),
                new BigDecimal("50.00"),
                "Address",
                "CASH_ON_DELIVERY",
                PaymentStatus.PENDING,
                OrderStatus.PENDING
        );

        when(orderRepository.findById("order-999")).thenReturn(Optional.of(otherCustomerOrder));

        // Customer "user-1" tries to access order belonging to "user-2"
        assertThrows(AccessDeniedException.class, () ->
                orderService.getOrderById("order-999", "user-1", false));
    }

    @Test
    @DisplayName("Should allow Admin to view any customer's order")
    void testAdminCanViewAnyOrder() {
        Order customerOrder = new Order(
                "order-999",
                "user-2",
                "Bob",
                new ArrayList<>(),
                new BigDecimal("50.00"),
                "Address",
                "CASH_ON_DELIVERY",
                PaymentStatus.PENDING,
                OrderStatus.PENDING
        );

        when(orderRepository.findById("order-999")).thenReturn(Optional.of(customerOrder));

        OrderResponse response = orderService.getOrderById("order-999", "admin-1", true);
        assertNotNull(response);
        assertEquals("order-999", response.getId());
    }

    @Test
    @DisplayName("Should allow valid order status transition")
    void testValidStatusTransition() {
        Order order = new Order(
                "order-1",
                "user-1",
                "Alice",
                new ArrayList<>(),
                new BigDecimal("25.00"),
                "Address",
                "CASH_ON_DELIVERY",
                PaymentStatus.PENDING,
                OrderStatus.PENDING
        );

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.updateOrderStatus("order-1", OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.getOrderStatus());
    }

    @Test
    @DisplayName("Should reject invalid order status transition")
    void testInvalidStatusTransition() {
        Order order = new Order(
                "order-1",
                "user-1",
                "Alice",
                new ArrayList<>(),
                new BigDecimal("25.00"),
                "Address",
                "CASH_ON_DELIVERY",
                PaymentStatus.PENDING,
                OrderStatus.DELIVERED // already delivered
        );

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));

        // Cannot transition from DELIVERED back to PENDING
        assertThrows(InvalidStatusTransitionException.class, () ->
                orderService.updateOrderStatus("order-1", OrderStatus.PENDING));

        verify(orderRepository, never()).save(any(Order.class));
    }
}
