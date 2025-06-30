package com.example.TpDA1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    
    // Información del usuario (opcional)
    private Long userId;
    private String username;
    
    // Información de la ruta (opcional)
    private Long routeId;
    private String routeOrigin;
    private String routeDestination;
    
    // Datos adicionales (JSON)
    private String data;
} 