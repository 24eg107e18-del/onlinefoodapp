package com.example.foodordering.service;

import com.example.foodordering.dto.CategoryRequest;
import com.example.foodordering.dto.CategoryResponse;
import com.example.foodordering.exception.DuplicateEmailException;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Category;
import com.example.foodordering.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActive(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        Category category = findCategoryById(id);
        return mapToResponse(category);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateEmailException("Category with name '" + name + "' already exists");
        }

        Category category = new Category(
                null,
                name,
                request.getDescription() != null ? request.getDescription().trim() : null,
                request.getImageUrl() != null ? request.getImageUrl().trim() : null,
                request.isActive()
        );

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        Category category = findCategoryById(id);
        String newName = request.getName().trim();

        if (!category.getName().equalsIgnoreCase(newName) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new DuplicateEmailException("Category with name '" + newName + "' already exists");
        }

        category.setName(newName);
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        category.setImageUrl(request.getImageUrl() != null ? request.getImageUrl().trim() : null);
        category.setActive(request.isActive());

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    public void deleteCategory(String id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    public Category findCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getImageUrl(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
