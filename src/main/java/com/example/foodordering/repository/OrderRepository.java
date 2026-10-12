package com.example.foodordering.repository;

import com.example.foodordering.model.Order;
import com.example.foodordering.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByCustomerIdOrderByOrderDateDesc(String customerId);
    Page<Order> findByCustomerIdOrderByOrderDateDesc(String customerId, Pageable pageable);
    List<Order> findAllByOrderByOrderDateDesc();
    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);
    long countByOrderStatus(OrderStatus orderStatus);
    long countByCustomerId(String customerId);
}
