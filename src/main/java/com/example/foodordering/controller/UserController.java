package com.example.foodordering.controller;

import com.example.foodordering.dto.UpdateProfileRequest;
import com.example.foodordering.dto.UserProfileResponse;
import com.example.foodordering.security.UserDetailsImpl;
import com.example.foodordering.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        UserProfileResponse profile = userService.getUserProfile(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(userDetails.getId(), request);
        return ResponseEntity.ok(profile);
    }
}
