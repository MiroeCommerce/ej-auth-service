package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.RegisterResponse;
import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.exception.PasswordConfirmNotMatchException;
import com.ecommerce.auth.exception.UserAlreadyExistsException;
import com.ecommerce.auth.mapper.UserMapper;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

// Add Logger to Service?
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final UserDetailsService userDetailsService;

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new PasswordConfirmNotMatchException("Passwords do not match");
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken", "username");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use", "email");
        }

        User user = userMapper.toUser(registerRequest);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role not set"));

        user.setRoles(Collections.singleton(userRole));
        user.setUserType("customer");
        user.setIsActive(true);
        user.setIsEmailVerified(false);
        User savedUser = userRepository.save(user);

        return new RegisterResponse("User registered successfully!",
                savedUser.getId(),
                savedUser.getUsername()
        );
    }

    // Use Redis here
    @Override
    public void logout(String token) {
        // Optionally implement token invalidation or blacklisting here
    }
}
