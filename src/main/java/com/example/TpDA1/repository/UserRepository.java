package com.example.TpDA1.repository;

import com.example.TpDA1.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationCode(String verificationCode);

    // Nuevo método para buscar usuarios por el token de recuperación de contraseña
    Optional<User> findByPasswordResetToken(String passwordResetToken);

    // bauti insano style
    Optional<User> findByUsername(String username);
}