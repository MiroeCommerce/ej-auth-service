package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
class RegistrationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void whenValidInput_thenReturns200() throws Exception {
        var request = new RegisterRequest("gooduser", "good@email.com", "password123", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void whenRegisterUser_thenSuccess() throws Exception {
        var registerRequest = new RegisterRequest("newuser2", "test2@example.com", "password123", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    void whenRegisterWithExistingUsername_thenConflict() throws Exception {
        Role userRole = roleRepository.findByName("ROLE_USER").get();

        User existingEmailUser = User
                .builder()
                .username("existinguser")
                .email("unique@example.com")
                .password(passwordEncoder.encode("pwd"))
                .roles(Collections.singleton(userRole))
                .userType("customer")
                .isActive(true)
                .isEmailVerified(false)
                .build();

        userRepository.save(existingEmailUser);

        var registerRequest = new RegisterRequest("existinguser", "test@example.com", "password123", "password123");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict()) // Expect 409 Conflict
                .andExpect(jsonPath("$.field").value("username"));
    }

    @Test
    void whenRegisterWithExistingEmail_thenConflict() throws Exception {
        Role userRole = roleRepository.findByName("ROLE_USER").get();

        User existingEmailUser = User
                .builder()
                .username("uniqueuser")
                .email("existing@example.com")
                .password(passwordEncoder.encode("pwd"))
                .roles(Collections.singleton(userRole))
                .userType("customer")
                .isActive(true)
                .isEmailVerified(false)
                .build();

        userRepository.save(existingEmailUser);

        var registerRequest = new RegisterRequest("newuser", "existing@example.com", "password123", "password123");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenPasswordsDoNotMatch_thenBadRequest() throws Exception {
        // Arrange
        var registerRequest = new RegisterRequest("newuser", "test@example.com", "password123", "password456");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({
            ", test@email.com, password123, password123", // Blank username
            "us, test@email.com, password123, password123", // Short username
            "longusernameistoolong, test@email.com, password123, password123", // Long username
            "testuser, invalid-email, password123, password123", // Invalid email
            "testuser, test@email.com, short, short" // Short password
    })
    void whenInvalidInput_thenReturns400(String username, String email, String password, String confirmPassword) throws Exception {
        var request = new RegisterRequest(username, email, password, confirmPassword);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}