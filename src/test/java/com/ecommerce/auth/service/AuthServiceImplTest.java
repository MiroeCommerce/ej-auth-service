package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.PasswordConfirmNotMatchException;
import com.ecommerce.auth.exception.UserAlreadyExistsException;
import com.ecommerce.auth.mapper.UserMapper;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void whenRegisterNewUser_thenSuccess() {
        // Arrange
        var request = new RegisterRequest("newuser", "test@example.com", "password123", "password123");
        User mockedUser = new User();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        when(userMapper.toUser(any(RegisterRequest.class))).thenReturn(mockedUser);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role()));
        when(userRepository.save(any(User.class))).thenReturn(new User());

        // Act
        authService.register(request);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void whenRegisterWithExistingUsername_thenThrowException() {
        // Arrange
        var request = new RegisterRequest("existinguser", "test@example.com", "password123", "password123");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(request);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenPasswordsDoNotMatch_thenThrowException() {
        // Arrange
        var request = new RegisterRequest("newuser", "test@example.com", "password123", "password456");

        // Act & Assert
        assertThrows(PasswordConfirmNotMatchException.class, () -> authService.register(request));

        verifyNoInteractions(userRepository, roleRepository, userMapper);
    }

    @Test
    void whenDefaultRoleNotFound_thenThrowException() {
        // Arrange
        var request = new RegisterRequest("newuser", "test@example.com", "password123", "password123");
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(any(RegisterRequest.class))).thenReturn(new User());

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }
}