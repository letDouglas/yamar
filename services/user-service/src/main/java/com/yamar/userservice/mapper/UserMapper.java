package com.yamar.userservice.mapper;

import com.yamar.userservice.dto.AddressRequest;
import com.yamar.userservice.dto.AddressResponse;
import com.yamar.userservice.dto.UserResponse;
import com.yamar.userservice.dto.UserUpdateRequest;
import com.yamar.userservice.model.Address;
import com.yamar.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        List<AddressResponse> addressResponses = (user.getAddresses() == null)
                ? Collections.emptyList()
                : user.getAddresses().stream()
                .map(this::toAddressResponse)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                addressResponses,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public AddressResponse toAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getZipCode(),
                address.getCountry(),
                address.getType()
        );
    }

    public Address toAddressEntity(AddressRequest request, User user) {
        return Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .type(request.getType())
                .user(user)
                .build();
    }

    public void updateUserFromRequest(User user, UserUpdateRequest request) {
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
    }
}