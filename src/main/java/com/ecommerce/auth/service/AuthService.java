package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.LoginRequest;
import com.ecommerce.auth.dto.LoginResponse;
import com.ecommerce.auth.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    void logout(String token);
}
