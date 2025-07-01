package com.example.TpDA1.controller;

import com.example.TpDA1.dto.*;
import com.example.TpDA1.model.User;
import com.example.TpDA1.responses.LoginResponse;
import com.example.TpDA1.service.AuthenticationService;
import com.example.TpDA1.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registeredUser = authenticationService.signup(registerUserDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = new LoginResponse(jwtToken, jwtService.getExpirationTime());
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account verified successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody ResendVerificationDto resendVerificationDto) {
        try {
            // Check which type of code to resend
            if ("passwordReset".equals(resendVerificationDto.getCodeType())) {
                authenticationService.generatePasswordResetCode(resendVerificationDto.getEmail());
                return ResponseEntity.ok(Collections.singletonMap("message",
                        "New password reset code sent to email: " + resendVerificationDto.getEmail()));
            } else {
                // Default to verification code
                authenticationService.resendVerificationCode(resendVerificationDto.getEmail());
                return ResponseEntity.ok(Collections.singletonMap("message",
                        "New verification code sent to email: " + resendVerificationDto.getEmail()));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Step 1: Request password reset code
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDto forgotPasswordDto) {
        try {
            authenticationService.generatePasswordResetCode(forgotPasswordDto.getEmail());
            return ResponseEntity.ok(Collections.singletonMap("message", 
                "Password reset code sent to email: " + forgotPasswordDto.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Step 2: Verify reset code
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody VerifyResetCodeDto verifyResetCodeDto) {
        try {
            boolean isValid = authenticationService.verifyPasswordResetCode(
                verifyResetCodeDto.getEmail(), 
                verifyResetCodeDto.getCode()
            );
            
            if (isValid) {
                return ResponseEntity.ok(Collections.singletonMap("verified", true));
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid code"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // Step 3: Reset password with new password and confirmation
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        try {
            // Validate passwords match
            if (!resetPasswordDto.getNewPassword().equals(resetPasswordDto.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Passwords do not match"));
            }

            authenticationService.resetPasswordWithCode(
                resetPasswordDto.getEmail(),
                resetPasswordDto.getCode(),
                resetPasswordDto.getNewPassword()
            );
            
            return ResponseEntity.ok(Collections.singletonMap("message", "Password has been reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    
    // GET /auth/profile - Obtener perfil (alias de /users/me)
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user);
    }
    
    // POST /auth/logout - Cerrar sesión
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // En una implementación real, aquí invalidarías el token
        // Por ahora solo retornamos un mensaje de éxito
        return ResponseEntity.ok(Collections.singletonMap("message", "Logout successful"));
    }
}