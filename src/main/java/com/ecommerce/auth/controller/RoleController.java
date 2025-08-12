package com.ecommerce.auth.controller;

import com.ecommerce.auth.entity.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class RoleController {

    /*
     1. GET    | /auth/roles
     2. GET    | /auth/roles/{id}
     3. POST   | /auth/roles
     4. PUT    | /auth/roles/{id}
     5. DELETE | /auth/roles/{id}
     6. GET    | /auth/users/{userId}/roles
     7. PUT    | /auth/users/{userId}/roles
    */

    /**
     * Currently mocked, doing nothing.
     */
    @GetMapping("/roles")
    public ResponseEntity<String> getRoles() {
        return ResponseEntity.ok().body("Roles found");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @GetMapping("/roles/{id}")
    public ResponseEntity<String> getRole(@PathVariable Long id) {
        return ResponseEntity.ok().body("Role found");
    }

    /**
     * Currently mocked, doing nothing.
     */
    // @PreAuthorize("hasRole('ADMIN')") TODO: Uncomment, when role-based authorization is implemented
    @PostMapping("/roles")
    public ResponseEntity<String> createRole(@RequestBody Role role) {
        return ResponseEntity.status(HttpStatus.CREATED).body("Role created");
    }

    /**
     * Currently mocked, doing nothing.
     */
    // @PreAuthorize("hasRole('ADMIN')") TODO: Uncomment, when role-based authorization is implemented
    @PutMapping("/roles/{id}")
    public ResponseEntity<String> editRole(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok().body("Role updated");
    }

    /**
     * Currently mocked, doing nothing.
     */
    // @PreAuthorize("hasRole('ADMIN')") TODO: Uncomment, when role-based authorization is implemented
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Role deleted");
    }

    /**
     * Currently mocked, doing nothing.
     */
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<String> getUserRoles(@PathVariable String userId) {
        return ResponseEntity.ok().body("User roles found");
    }

    /**
     * Currently mocked, doing nothing.
     */
    // @PreAuthorize("hasRole('ADMIN')") TODO: Uncomment, when role-based authorization is implemented
    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<String> editUserRoles(@PathVariable String userId, @RequestBody List<Role> roles) {
        return ResponseEntity.ok().body("User roles updated");
    }

}
