package com.ecommerce.auth.controller;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CacheService cacheService;

    /*
     1. POST | /auth/register
     2. POST | /auth/logout
     3. POST | /auth/login
     4. GET  | /auth/session
     5. POST | /auth/refresh-token
    */

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.status(HttpStatus.OK).body("User logged successfully");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.status(HttpStatus.OK).body("User logged out");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @GetMapping("/session")
    public ResponseEntity<String> session() {
        return ResponseEntity.status(HttpStatus.OK).body("Session information");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken() {
        return ResponseEntity.status(HttpStatus.OK).body("Refresh Token successfully");
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
