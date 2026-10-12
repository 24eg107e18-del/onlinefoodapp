package com.example.foodordering.repository;

import com.example.foodordering.model.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends MongoRepository<FoodItem, String> {
    List<FoodItem> findByCategoryId(String categoryId);
    List<FoodItem> findByCategoryIdAndAvailable(String categoryId, boolean available);
    List<FoodItem> findByAvailable(boolean available);

    Page<FoodItem> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<FoodItem> findByCategoryId(String categoryId, Pageable pageable);
    Page<FoodItem> findByCategoryIdAndNameContainingIgnoreCase(String categoryId, String name, Pageable pageable);
    Page<FoodItem> findByAvailable(boolean available, Pageable pageable);
    Page<FoodItem> findByAvailableAndNameContainingIgnoreCase(boolean available, String name, Pageable pageable);
    Page<FoodItem> findByCategoryIdAndAvailable(String categoryId, boolean available, Pageable pageable);
    Page<FoodItem> findByCategoryIdAndAvailableAndNameContainingIgnoreCase(String categoryId, boolean available, String name, Pageable pageable);
}
