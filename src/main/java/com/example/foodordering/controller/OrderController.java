package com.example.foodordering.controller;

import com.example.foodordering.dto.CheckoutRequest;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.dto.OrderStatusUpdateRequest;
import com.example.foodordering.model.Role;
import com.example.foodordering.security.UserDetailsImpl;
import com.example.foodordering.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.placeOrder(userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<OrderResponse> orders = orderService.getCustomerOrders(userDetails.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String id) {
        boolean isAdmin = userDetails.getRole() == Role.ADMIN;
        OrderResponse response = orderService.getOrderById(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
