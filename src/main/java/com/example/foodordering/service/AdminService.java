package com.example.foodordering.service;

import com.example.foodordering.dto.AdminDashboardResponse;
import com.example.foodordering.model.Order;
import com.example.foodordering.model.OrderStatus;
import com.example.foodordering.model.Role;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import com.example.foodordering.repository.OrderRepository;
import com.example.foodordering.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;

    public AdminService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        FoodItemRepository foodItemRepository,
                        CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodItemRepository = foodItemRepository;
        this.categoryRepository = categoryRepository;
    }

    public AdminDashboardResponse getDashboardStats() {
        long totalOrders = orderRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalFoodItems = foodItemRepository.count();
        long totalCategories = categoryRepository.count();

        List<Order> allOrders = orderRepository.findAll();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Long> statusCounts = new HashMap<>();

        for (OrderStatus status : OrderStatus.values()) {
            statusCounts.put(status.name(), 0L);
        }

        for (Order order : allOrders) {
            if (order.getOrderStatus() != null) {
                statusCounts.put(order.getOrderStatus().name(),
                        statusCounts.getOrDefault(order.getOrderStatus().name(), 0L) + 1L);
            }
            // Revenue only counted for non-cancelled orders
            if (order.getOrderStatus() != OrderStatus.CANCELLED && order.getTotalAmount() != null) {
                totalRevenue = totalRevenue.add(order.getTotalAmount());
            }
        }

        return new AdminDashboardResponse(
                totalOrders,
                totalCustomers,
                totalFoodItems,
                totalCategories,
                totalRevenue,
                statusCounts
        );
    }
}
