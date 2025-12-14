package com.yamar.userservice.service;

import com.yamar.userservice.dto.AddressRequest;
import com.yamar.userservice.dto.UserResponse;
import com.yamar.userservice.dto.UserUpdateRequest;
import com.yamar.userservice.exception.UserNotFoundException;
import com.yamar.userservice.mapper.UserMapper;
import com.yamar.userservice.model.Address;
import com.yamar.userservice.model.User;
import com.yamar.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository repository;
    private final UserMapper mapper;

    @Transactional
    public UserResponse syncUser(Jwt jwt) {
        String auth0Id = jwt.getSubject();
        String email = jwt.getClaimAsString("email");

        User user = repository.findById(auth0Id)
                .orElseGet(() -> {
                    log.info("Ghost User Sync: Creating new local profile for {}", auth0Id);
                    User newUser = User.builder()
                            .id(auth0Id)
                            .email(email)
                            .build();
                    return repository.save(newUser);
                });

        return mapper.toUserResponse(user);
    }

    public UserResponse getMe(String id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return mapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        mapper.updateUserFromRequest(user, request);
        User savedUser = repository.save(user);

        log.info("Profile updated for user {}", id);
        return mapper.toUserResponse(savedUser);
    }

    @Transactional
    public UserResponse addAddress(String id, AddressRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        Address address = mapper.toAddressEntity(request, user);
        user.getAddresses().add(address);

        User savedUser = repository.save(user);
        log.info("Address added for user {}", id);
        return mapper.toUserResponse(savedUser);
    }
}