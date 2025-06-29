package com.example.TpDA1.controller;

import com.example.TpDA1.dto.PushTokenDto;
import com.example.TpDA1.model.User;
import com.example.TpDA1.service.AuthenticationService;
import com.example.TpDA1.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
public class UserController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    public UserController(UserService userService, AuthenticationService authenticationService) { //Esto me puede romper
        this.userService = userService;
        this.authenticationService = authenticationService;
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
        List <User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/push-token")
    public ResponseEntity<?> savePushToken(@RequestBody PushTokenDto pushTokenDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            User currentUser = (User) authentication.getPrincipal();

            authenticationService.savePushToken(currentUser, pushTokenDto.getToken());

            return ResponseEntity.ok("Push token guardado");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error guardando push token");
        }
    }
}