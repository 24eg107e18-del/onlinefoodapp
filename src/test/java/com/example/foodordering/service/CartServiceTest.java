package com.example.foodordering.service;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.exception.InvalidOrderException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CartRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private FoodItemRepository foodItemRepository;

    @InjectMocks
    private CartService cartService;

    private FoodItem availableFood;
    private FoodItem unavailableFood;
    private Cart customerCart;

    @BeforeEach
    void setUp() {
        availableFood = new FoodItem(
                "food-1",
                "Pepperoni Pizza",
                "Delicious pizza",
                "cat-1",
                new BigDecimal("20.00"),
                "http://img.com/pizza.jpg",
                true
        );

        unavailableFood = new FoodItem(
                "food-2",
                "Seasonal Truffle Pizza",
                "Out of season",
                "cat-1",
                new BigDecimal("25.00"),
                "http://img.com/truffle.jpg",
                false
        );

        customerCart = new Cart("cust-1");
    }

    @Test
    @DisplayName("Should successfully add available food item to customer cart and calculate totals")
    void testAddToCartSuccess() {
        AddToCartRequest request = new AddToCartRequest("food-1", 2);

        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(customerCart));
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(availableFood));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        when(foodItemRepository.findAllById(any())).thenReturn(List.of(availableFood));

        CartResponse response = cartService.addToCart("cust-1", request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(2, response.getTotalQuantity());
        // Subtotal = 2 * 20.00 = 40.00
        assertEquals(new BigDecimal("40.00"), response.getSubtotal());
        // Delivery fee: >= 40.00 is free (0.00)
        assertEquals(new BigDecimal("0.00"), response.getDeliveryFee());
        // Tax: 5% of 40.00 = 2.00
        assertEquals(new BigDecimal("2.00"), response.getTax());
        // Total = 40.00 + 0.00 + 2.00 = 42.00
        assertEquals(new BigDecimal("42.00"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should reject adding unavailable food item to cart")
    void testAddToCartUnavailableItem() {
        AddToCartRequest request = new AddToCartRequest("food-2", 1);

        when(foodItemRepository.findById("food-2")).thenReturn(Optional.of(unavailableFood));

        assertThrows(InvalidOrderException.class, () -> cartService.addToCart("cust-1", request));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should apply standard delivery fee when subtotal is below threshold")
    void testDeliveryFeeApplied() {
        AddToCartRequest request = new AddToCartRequest("food-1", 1); // 1 * 20 = 20.00

        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(customerCart));
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(availableFood));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        when(foodItemRepository.findAllById(any())).thenReturn(List.of(availableFood));

        CartResponse response = cartService.addToCart("cust-1", request);

        // Subtotal = 20.00
        assertEquals(new BigDecimal("20.00"), response.getSubtotal());
        // Delivery fee = 3.50
        assertEquals(new BigDecimal("3.50"), response.getDeliveryFee());
        // Tax = 5% of 20.00 = 1.00
        assertEquals(new BigDecimal("1.00"), response.getTax());
        // Total = 20.00 + 3.50 + 1.00 = 24.50
        assertEquals(new BigDecimal("24.50"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should remove item when quantity is set to zero")
    void testUpdateCartItemQuantityZeroRemovesItem() {
        customerCart.getItems().add(new com.example.foodordering.model.CartItem("food-1", 2));

        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(customerCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse response = cartService.updateCartItemQuantity("cust-1", "food-1", 0);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalQuantity());
        assertEquals(new BigDecimal("0.00"), response.getTotalAmount());
    }

    @Test
    @DisplayName("Should clear all items from the cart")
    void testClearCart() {
        customerCart.getItems().add(new com.example.foodordering.model.CartItem("food-1", 2));
        when(cartRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(customerCart));

        cartService.clearCart("cust-1");

        assertTrue(customerCart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(customerCart);
    }
}
