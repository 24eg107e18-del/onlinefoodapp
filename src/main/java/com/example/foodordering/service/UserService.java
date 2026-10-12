package com.example.foodordering.service;

import com.example.foodordering.dto.UpdateProfileRequest;
import com.example.foodordering.dto.UserProfileResponse;
import com.example.foodordering.exception.ResourceNotFoundException;
import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileResponse getUserProfile(String userId) {
        User user = findUserById(userId);
        return mapToProfileResponse(user);
    }

    public UserProfileResponse updateUserProfile(String userId, UpdateProfileRequest request) {
        User user = findUserById(userId);
        user.setName(request.getName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        user.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    public List<UserProfileResponse> getAllCustomers() {
        return userRepository.findByRole(Role.CUSTOMER)
                .stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    public Page<UserProfileResponse> getAllCustomers(Pageable pageable) {
        return userRepository.findByRole(Role.CUSTOMER, pageable)
                .map(this::mapToProfileResponse);
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public UserProfileResponse mapToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
