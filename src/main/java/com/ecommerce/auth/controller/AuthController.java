package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.RegisterResponse;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CacheService cacheService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.register(registerRequest);
        return ResponseEntity.ok(registerResponse);
    }

    @PostMapping("/mock-login")
    public Map<String, String> mockLogin() {
        // 1. Simulate a user.
        String fakeUserId = "1";

        // 2. Generate fake tokens.
        String fakeAccessToken = UUID.randomUUID().toString();
        String fakeRefreshToken = UUID.randomUUID().toString();

        // 3. Save to Redis.
        this.cacheService.create(fakeUserId, fakeRefreshToken);

        // 4. Return result.
        return Map.of(
                "userId", fakeUserId,
                "accessToken", fakeAccessToken,
                "refreshToken", fakeRefreshToken
        );
    }

    @GetMapping("/cache/{key}")
    public Map<String, String> mockRefreshToken(@PathVariable String key) {
        return Map.of(key, this.cacheService.read(key).toString());
    }

}
