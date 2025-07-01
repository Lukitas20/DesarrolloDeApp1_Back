package com.example.TpDA1.controller;

import com.example.TpDA1.model.User;
import com.example.TpDA1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        try {
            System.out.println("🔄 UserController - Obteniendo usuario autenticado...");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("❌ UserController - Usuario no autenticado");
                return ResponseEntity.status(401).build();
            }

            User currentUser = (User) authentication.getPrincipal();
            System.out.println("✅ UserController - Usuario obtenido: " + currentUser.getUsername());
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            System.err.println("❌ UserController - Error al obtener usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }
}