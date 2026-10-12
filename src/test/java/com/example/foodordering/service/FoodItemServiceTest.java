package com.example.foodordering.service;

import com.example.foodordering.dto.FoodItemRequest;
import com.example.foodordering.dto.FoodItemResponse;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Category;
import com.example.foodordering.model.FoodItem;
import com.example.foodordering.repository.CategoryRepository;
import com.example.foodordering.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodItemServiceTest {

    @Mock
    private FoodItemRepository foodItemRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FoodItemService foodItemService;

    private Category testCategory;
    private FoodItem testFoodItem;

    @BeforeEach
    void setUp() {
        testCategory = new Category("cat-1", "Pizza", "Tasty pizzas", "http://img.com/pizza.jpg", true);
        testFoodItem = new FoodItem(
                "food-1",
                "Margherita Pizza",
                "Fresh mozzarella, tomato sauce and basil",
                "cat-1",
                new BigDecimal("12.99"),
                "http://img.com/margherita.jpg",
                true
        );
    }

    @Test
    @DisplayName("Should successfully create a food item when category exists")
    void testCreateFoodItemSuccess() {
        FoodItemRequest request = new FoodItemRequest(
                "Margherita Pizza",
                "Fresh mozzarella, tomato sauce and basil",
                "cat-1",
                new BigDecimal("12.99"),
                "http://img.com/margherita.jpg",
                true
        );

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(foodItemRepository.save(any(FoodItem.class))).thenReturn(testFoodItem);

        FoodItemResponse response = foodItemService.createFoodItem(request);

        assertNotNull(response);
        assertEquals("food-1", response.getId());
        assertEquals("Margherita Pizza", response.getName());
        assertEquals(new BigDecimal("12.99"), response.getPrice());
        assertEquals("Pizza", response.getCategoryName());
    }

    @Test
    @DisplayName("Should reject food item creation if category does not exist")
    void testCreateFoodItemInvalidCategory() {
        FoodItemRequest request = new FoodItemRequest(
                "Margherita Pizza",
                "Description",
                "invalid-cat",
                new BigDecimal("12.99"),
                "http://img.com/margherita.jpg",
                true
        );

        when(categoryRepository.findById("invalid-cat")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> foodItemService.createFoodItem(request));
        verify(foodItemRepository, never()).save(any(FoodItem.class));
    }

    @Test
    @DisplayName("Should reject food item creation with negative or zero price")
    void testCreateFoodItemInvalidPrice() {
        FoodItemRequest request = new FoodItemRequest(
                "Free Pizza",
                "Description",
                "cat-1",
                new BigDecimal("0.00"),
                "http://img.com/margherita.jpg",
                true
        );

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));

        assertThrows(IllegalArgumentException.class, () -> foodItemService.createFoodItem(request));
        verify(foodItemRepository, never()).save(any(FoodItem.class));
    }

    @Test
    @DisplayName("Should search and filter food items correctly")
    void testSearchAndFilterFoodItems() {
        when(foodItemRepository.findByCategoryIdAndAvailable("cat-1", true))
                .thenReturn(List.of(testFoodItem));
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        List<FoodItemResponse> results = foodItemService.getAllFoodItems("margherita", "cat-1", true);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Margherita Pizza", results.get(0).getName());
    }

    @Test
    @DisplayName("Should delete food item when found")
    void testDeleteFoodItem() {
        when(foodItemRepository.findById("food-1")).thenReturn(Optional.of(testFoodItem));

        foodItemService.deleteFoodItem("food-1");

        verify(foodItemRepository, times(1)).delete(testFoodItem);
    }
}
