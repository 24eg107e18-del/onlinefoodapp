package com.example.foodordering.service;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.CartItemResponse;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.exception.InvalidOrderException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Cart;
import com.example.foodordering.model.CartItem;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CartRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal FREE_DELIVERY_THRESHOLD = new BigDecimal("40.00");
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("3.50");

    private final CartRepository cartRepository;
    private final FoodItemRepository foodItemRepository;

    public CartService(CartRepository cartRepository, FoodItemRepository foodItemRepository) {
        this.cartRepository = cartRepository;
        this.foodItemRepository = foodItemRepository;
    }

    public Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart newCart = new Cart(customerId);
                    return cartRepository.save(newCart);
                });
    }

    public CartResponse getCartResponse(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(String customerId, AddToCartRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + request.getFoodItemId()));

        if (!foodItem.isAvailable()) {
            throw new InvalidOrderException("The selected item '" + foodItem.getName() + "' is currently unavailable");
        }

        Cart cart = getOrCreateCart(customerId);
        List<CartItem> items = cart.getItems();

        Optional<CartItem> existingItem = items.stream()
                .filter(i -> i.getFoodItemId().equals(request.getFoodItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            items.add(new CartItem(request.getFoodItemId(), request.getQuantity()));
        }

        cart.setUpdatedAt(Instant.now());
        Cart saved = cartRepository.save(cart);
        return buildCartResponse(saved);
    }

    public CartResponse updateCartItemQuantity(String customerId, String foodItemId, int quantity) {
        Cart cart = getOrCreateCart(customerId);

        if (quantity <= 0) {
            cart.getItems().removeIf(i -> i.getFoodItemId().equals(foodItemId));
        } else {
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + foodItemId));

            if (!foodItem.isAvailable()) {
                throw new InvalidOrderException("Item '" + foodItem.getName() + "' is currently out of stock");
            }

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(i -> i.getFoodItemId().equals(foodItemId))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(quantity);
            } else {
                cart.getItems().add(new CartItem(foodItemId, quantity));
            }
        }

        cart.setUpdatedAt(Instant.now());
        Cart saved = cartRepository.save(cart);
        return buildCartResponse(saved);
    }

    public CartResponse removeFromCart(String customerId, String foodItemId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().removeIf(i -> i.getFoodItemId().equals(foodItemId));
        cart.setUpdatedAt(Instant.now());
        Cart saved = cartRepository.save(cart);
        return buildCartResponse(saved);
    }

    public void clearCart(String customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        cart.setUpdatedAt(Instant.now());
        cartRepository.save(cart);
    }

    public CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            List<String> foodIds = cart.getItems().stream()
                    .map(CartItem::getFoodItemId)
                    .collect(Collectors.toList());

            Map<String, FoodItem> foodMap = foodItemRepository.findAllById(foodIds).stream()
                    .collect(Collectors.toMap(FoodItem::getId, f -> f));

            for (CartItem ci : cart.getItems()) {
                FoodItem food = foodMap.get(ci.getFoodItemId());
                if (food != null) {
                    BigDecimal lineTotal = food.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);
                    subtotal = subtotal.add(lineTotal);
                    totalQuantity += ci.getQuantity();

                    itemResponses.add(new CartItemResponse(
                            food.getId(),
                            food.getName(),
                            food.getPrice(),
                            food.getImageUrl(),
                            ci.getQuantity(),
                            lineTotal,
                            food.isAvailable()
                    ));
                }
            }
        }

        BigDecimal deliveryFee = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
            deliveryFee = subtotal.compareTo(FREE_DELIVERY_THRESHOLD) >= 0 ? BigDecimal.ZERO : STANDARD_DELIVERY_FEE;
            tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
            totalAmount = subtotal.add(deliveryFee).add(tax).setScale(2, RoundingMode.HALF_UP);
        }

        return new CartResponse(
                cart.getCustomerId(),
                itemResponses,
                subtotal.setScale(2, RoundingMode.HALF_UP),
                deliveryFee.setScale(2, RoundingMode.HALF_UP),
                tax.setScale(2, RoundingMode.HALF_UP),
                totalAmount.setScale(2, RoundingMode.HALF_UP),
                totalQuantity
        );
    }
}
