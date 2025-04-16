package com.example.TpDA1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteHistoryDto {
    private Long routeId;
    private String origin;
    private String destination;
    private Double distance;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String completionTime; // Formato "X horas Y minutos"
    private Double payment;
    
    // Campos adicionales que podrían ser útiles
    private String status;
    private Double averageSpeed; // km/h
}