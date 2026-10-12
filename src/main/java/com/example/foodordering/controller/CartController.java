package com.example.foodordering.controller;

import com.example.foodordering.dto.AddToCartRequest;
import com.example.foodordering.dto.CartResponse;
import com.example.foodordering.dto.UpdateCartItemRequest;
import com.example.foodordering.security.UserDetailsImpl;
import com.example.foodordering.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        CartResponse cart = cartService.getCartResponse(userDetails.getId());
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(userDetails.getId(), request);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse cart = cartService.updateCartItemQuantity(userDetails.getId(), itemId, request.getQuantity());
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String itemId) {
        CartResponse cart = cartService.removeFromCart(userDetails.getId(), itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartService.clearCart(userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
