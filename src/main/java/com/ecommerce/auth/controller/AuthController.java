package com.ecommerce.auth.controller;

import com.ecommerce.auth.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

}
