package com.example.foodordering.controller;

import com.example.foodordering.dto.AdminDashboardResponse;
import com.example.foodordering.dto.OrderResponse;
import com.example.foodordering.dto.UserProfileResponse;
import com.example.foodordering.service.AdminService;
import com.example.foodordering.service.OrderService;
import com.example.foodordering.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminController(AdminService adminService, OrderService orderService, UserService userService) {
        this.adminService = adminService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
            Page<OrderResponse> orders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(orders);
        }
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<UserProfileResponse> users = userService.getAllCustomers(pageable);
            return ResponseEntity.ok(users);
        }
        List<UserProfileResponse> users = userService.getAllCustomers();
        return ResponseEntity.ok(users);
    }
}
