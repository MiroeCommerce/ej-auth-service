package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.RegisterRequest;
import com.ecommerce.auth.dto.RegisterResponse;
import com.ecommerce.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     1. POST | /auth/register
     2. POST | /auth/logout
     3. POST | /auth/login
     4. GET  | /auth/session
     5. POST | /auth/refresh-token
    */

    /**
     * Working on it
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authService.register(registerRequest);
        return ResponseEntity.ok(registerResponse);
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

}
