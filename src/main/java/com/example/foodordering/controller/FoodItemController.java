package com.example.foodordering.controller;

import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @GetMapping
    public ResponseEntity<?> getAllFoods(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        if (page != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<FoodItemResponse> pagedFoods = foodItemService.getPagedFoodItems(search, category, available, pageable);
            return ResponseEntity.ok(pagedFoods);
        }

        List<FoodItemResponse> foods = foodItemService.getAllFoodItems(search, category, available);
        return ResponseEntity.ok(foods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodItemResponse> getFoodById(@PathVariable String id) {
        FoodItemResponse food = foodItemService.getFoodItemById(id);
        return ResponseEntity.ok(food);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FoodItemResponse> createFood(@Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse created = foodItemService.createFoodItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FoodItemResponse> updateFood(
            @PathVariable String id,
            @Valid @RequestBody FoodItemRequest request) {
        FoodItemResponse updated = foodItemService.updateFoodItem(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFood(@PathVariable String id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.noContent().build();
    }
}
