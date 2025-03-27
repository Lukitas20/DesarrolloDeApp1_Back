package com.example.TpDA1.service;

import com.example.TpDA1.dto.LoginUserDto;
import com.example.TpDA1.dto.RegisterUserDto;
import com.example.TpDA1.dto.VerifyUserDto;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User signup(RegisterUserDto input) {
        Optional<User> existingUser = userRepository.findByEmail(input.getEmail());

        if (existingUser.isPresent()) {
            throw new RuntimeException("The email is already registered.");
        }

        User user = new User(input.getUsername(), input.getEmail(), passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Please verify your account.");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Invalid verification code");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }

    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new RuntimeException("Account is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Generar un código de recuperación de contraseña
    public String generatePasswordResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generar un código de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setPasswordResetToken(code);
        user.setPasswordResetTokenExpiresAt(LocalDateTime.now().plusMinutes(15)); // Código válido por 15 minutos
        userRepository.save(user);

        // Enviar el código por correo
        sendPasswordResetEmail(user, code);

        return code;
    }

    // Restablecer la contraseña usando el código
    public void resetPasswordWithCode(String email, String code, String newPassword) {
        // Buscar al usuario por correo electrónico
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    
        // Validar el código de recuperación
        if (user.getPasswordResetToken() == null || !user.getPasswordResetToken().equals(code)) {
            throw new RuntimeException("Invalid code");
        }
    
        // Validar si el código ha expirado
        if (user.getPasswordResetTokenExpiresAt() == null || user.getPasswordResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code has expired");
        }
    
        // Actualizar la contraseña del usuario
        user.setPassword(passwordEncoder.encode(newPassword));
    
        // Limpiar el código de recuperación y su fecha de expiración
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiresAt(null);
    
        // Guardar los cambios en la base de datos
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String body = "Your verification code is: " + verificationCode;
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendPasswordResetEmail(User user, String code) {
        String subject = "Password Reset Request";
        String body = "Use the following code to reset your password: " + code;
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, body);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}