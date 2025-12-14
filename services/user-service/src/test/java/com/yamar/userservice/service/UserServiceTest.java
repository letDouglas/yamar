package com.yamar.userservice.service;

import com.yamar.userservice.dto.AddressRequest;
import com.yamar.userservice.dto.UserResponse;
import com.yamar.userservice.dto.UserUpdateRequest;
import com.yamar.userservice.exception.UserNotFoundException;
import com.yamar.userservice.mapper.UserMapper;
import com.yamar.userservice.model.Address;
import com.yamar.userservice.model.AddressType;
import com.yamar.userservice.model.User;
import com.yamar.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserService userService;

    private final String AUTH0_ID = "auth0|123456";
    private final String EMAIL = "test@yamar.com";

    /**
     * Tests that a new user is created (saved) when not found in DB.
     */
    @Test
    void syncUser_shouldCreateUser_whenUserDoesNotExist() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(AUTH0_ID);
        when(jwt.getClaimAsString("email")).thenReturn(EMAIL);

        when(repository.findById(AUTH0_ID)).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(mapper.toUserResponse(any(User.class))).thenReturn(new UserResponse(AUTH0_ID, EMAIL, null, null, null, null, null, null));

        UserResponse response = userService.syncUser(jwt);

        assertNotNull(response);
        assertEquals(AUTH0_ID, response.id());
        verify(repository).save(any(User.class));
    }

    /**
     * Tests that existing user is returned without saving.
     */
    @Test
    void syncUser_shouldReturnExistingUser_whenUserExists() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(AUTH0_ID);

        User existingUser = User.builder().id(AUTH0_ID).email(EMAIL).build();
        when(repository.findById(AUTH0_ID)).thenReturn(Optional.of(existingUser));
        when(mapper.toUserResponse(existingUser)).thenReturn(new UserResponse(AUTH0_ID, EMAIL, null, null, null, null, null, null));

        userService.syncUser(jwt);

        verify(repository, never()).save(any());
    }

    @Test
    void getMe_shouldThrowException_whenUserNotFound() {
        when(repository.findById(AUTH0_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getMe(AUTH0_ID));
    }

    @Test
    void addAddress_shouldAddAddressAndSave() {
        User user = User.builder().id(AUTH0_ID).addresses(new ArrayList<>()).build();
        AddressRequest request = new AddressRequest();
        request.setType(AddressType.SHIPPING);

        when(repository.findById(AUTH0_ID)).thenReturn(Optional.of(user));
        when(mapper.toAddressEntity(request, user)).thenReturn(new Address());
        when(repository.save(user)).thenReturn(user);

        userService.addAddress(AUTH0_ID, request);

        assertEquals(1, user.getAddresses().size());
        verify(repository).save(user);
    }

    @Test
    void updateUser_shouldCallMapperAndSave() {
        User user = User.builder().id(AUTH0_ID).build();
        UserUpdateRequest request = new UserUpdateRequest();

        when(repository.findById(AUTH0_ID)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        userService.updateUser(AUTH0_ID, request);

        verify(mapper).updateUserFromRequest(user, request);
        verify(repository).save(user);
    }
}