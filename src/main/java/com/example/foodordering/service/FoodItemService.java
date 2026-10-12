package com.example.foodordering.service;

import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Category;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FoodItemService {

    private final FoodItemRepository foodItemRepository;
    private final CategoryRepository categoryRepository;

    public FoodItemService(FoodItemRepository foodItemRepository, CategoryRepository categoryRepository) {
        this.foodItemRepository = foodItemRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<FoodItemResponse> getAllFoodItems(String search, String categoryId, Boolean available) {
        List<FoodItem> items;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasCategory = StringUtils.hasText(categoryId);

        if (hasCategory && available != null) {
            items = foodItemRepository.findByCategoryIdAndAvailable(categoryId, available);
        } else if (hasCategory) {
            items = foodItemRepository.findByCategoryId(categoryId);
        } else if (available != null) {
            items = foodItemRepository.findByAvailable(available);
        } else {
            items = foodItemRepository.findAll();
        }

        if (hasSearch) {
            String lowerSearch = search.trim().toLowerCase();
            items = items.stream()
                    .filter(item -> item.getName().toLowerCase().contains(lowerSearch) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerSearch)))
                    .collect(Collectors.toList());
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        return items.stream()
                .map(item -> mapToResponse(item, categoryNames.getOrDefault(item.getCategoryId(), "Unknown")))
                .collect(Collectors.toList());
    }

    public Page<FoodItemResponse> getPagedFoodItems(String search, String categoryId, Boolean available, Pageable pageable) {
        Page<FoodItem> page;
        boolean hasSearch = StringUtils.hasText(search);
        boolean hasCategory = StringUtils.hasText(categoryId);

        if (hasCategory && available != null && hasSearch) {
            page = foodItemRepository.findByCategoryIdAndAvailableAndNameContainingIgnoreCase(categoryId, available, search.trim(), pageable);
        } else if (hasCategory && available != null) {
            page = foodItemRepository.findByCategoryIdAndAvailable(categoryId, available, pageable);
        } else if (hasCategory && hasSearch) {
            page = foodItemRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, search.trim(), pageable);
        } else if (hasCategory) {
            page = foodItemRepository.findByCategoryId(categoryId, pageable);
        } else if (available != null && hasSearch) {
            page = foodItemRepository.findByAvailableAndNameContainingIgnoreCase(available, search.trim(), pageable);
        } else if (available != null) {
            page = foodItemRepository.findByAvailable(available, pageable);
        } else if (hasSearch) {
            page = foodItemRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            page = foodItemRepository.findAll(pageable);
        }

        Map<String, String> categoryNames = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));

        return page.map(item -> mapToResponse(item, categoryNames.getOrDefault(item.getCategoryId(), "Unknown")));
    }

    public FoodItemResponse getFoodItemById(String id) {
        FoodItem item = findFoodById(id);
        String categoryName = categoryRepository.findById(item.getCategoryId())
                .map(Category::getName)
                .orElse("Unknown");
        return mapToResponse(item, categoryName);
    }

    public FoodItemResponse createFoodItem(FoodItemRequest request) {
        // Validate Category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Food price must be greater than zero");
        }

        FoodItem item = new FoodItem(
                null,
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : null,
                category.getId(),
                request.getPrice(),
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                request.getAvailable() != null ? request.getAvailable() : true
        );

        FoodItem saved = foodItemRepository.save(item);
        return mapToResponse(saved, category.getName());
    }

    public FoodItemResponse updateFoodItem(String id, FoodItemRequest request) {
        FoodItem item = findFoodById(id);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Food price must be greater than zero");
        }

        item.setName(request.getName().trim());
        item.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        item.setCategoryId(category.getId());
        item.setPrice(request.getPrice());
        item.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }
        item.setUpdatedAt(Instant.now());

        FoodItem updated = foodItemRepository.save(item);
        return mapToResponse(updated, category.getName());
    }

    public void deleteFoodItem(String id) {
        FoodItem item = findFoodById(id);
        foodItemRepository.delete(item);
    }

    public FoodItem findFoodById(String id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food item not found with ID: " + id));
    }

    private FoodItemResponse mapToResponse(FoodItem item, String categoryName) {
        return new FoodItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategoryId(),
                categoryName,
                item.getPrice(),
                item.getImageUrl(),
                item.isAvailable(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
