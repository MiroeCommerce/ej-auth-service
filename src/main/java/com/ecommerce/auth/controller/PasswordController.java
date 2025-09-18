package com.ecommerce.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class PasswordController {

    /*
     1. POST | /auth/forgot-password
     2. POST | /auth/reset-password
     3. POST | /auth/change-password
    */

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword() {
        return ResponseEntity.ok("Forgot password");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword() {
        return ResponseEntity.ok("Reset password");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword() {
        return ResponseEntity.ok("Change password");
    }

}
