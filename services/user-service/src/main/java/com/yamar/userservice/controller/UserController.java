package com.yamar.userservice.controller;

import com.yamar.userservice.dto.AddressRequest;
import com.yamar.userservice.dto.UserResponse;
import com.yamar.userservice.dto.UserUpdateRequest;
import com.yamar.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sync")
    public ResponseEntity<UserResponse> syncUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.syncUser(jwt));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getMe(jwt.getSubject()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(jwt.getSubject(), request));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<UserResponse> addAddress(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userService.addAddress(jwt.getSubject(), request));
    }
}