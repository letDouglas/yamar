package com.yamar.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yamar.userservice.dto.AddressRequest;
import com.yamar.userservice.dto.UserResponse;
import com.yamar.userservice.model.AddressType;
import com.yamar.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for UserController.
 * Uses @MockitoBean (Spring Boot 3.4+) or @MockBean (older) to mock the service.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String AUTH0_ID = "auth0|123";

    @Test
    void syncUser_shouldReturn200_whenAuthorized() throws Exception {
        UserResponse mockResponse = new UserResponse(AUTH0_ID, "test@test.com", null, null, null, null, null, null);
        when(userService.syncUser(any())).thenReturn(mockResponse);

        mockMvc.perform(post("/users/sync")
                        .with(jwt().jwt(builder -> builder.subject(AUTH0_ID).claim("email", "test@test.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AUTH0_ID))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void getMe_shouldReturn401_whenNoJwt() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addAddress_shouldProcessJsonCorrectly() throws Exception {
        AddressRequest request = new AddressRequest();
        request.setCity("Milan");
        request.setType(AddressType.SHIPPING);

        mockMvc.perform(post("/users/me/addresses")
                        .with(jwt().jwt(builder -> builder.subject(AUTH0_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}