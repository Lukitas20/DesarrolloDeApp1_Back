package com.example.TpDA1.service;

import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;

    // Get all available routes (not assigned to any driver)
    public List<Route> getAvailableRoutes() {
        return routeRepository.findByStatus("AVAILABLE");
    }

    // Get routes assigned to a specific driver
    public List<Route> getDriverRoutes(User driver) {
        return routeRepository.findByDriver(driver);
    }

    // Assign a route to a driver
    public Route assignRouteToDriver(Long routeId, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (!"AVAILABLE".equals(route.getStatus())) {
            throw new RuntimeException("Route is not available for assignment");
        }

        route.setDriver(driver);
        route.setStatus("ASSIGNED");
        route.setAssignedAt(LocalDateTime.now());

        return routeRepository.save(route);
    }

    // Update route status
    public Route updateRouteStatus(Long routeId, String status, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Route is not assigned to this driver");
        }

        route.setStatus(status);

        if ("COMPLETED".equals(status)) {
            route.setCompletedAt(LocalDateTime.now());
        }

        return routeRepository.save(route);
    }
    
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route completeRoute(Long routeId, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("This route is not assigned to you");
        }
        
        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("Only assigned routes can be completed");
        }
        
        route.setStatus("COMPLETED");
        route.setCompletedAt(LocalDateTime.now());
        return routeRepository.save(route);
    }

    public List<RouteHistoryDto> getDriverRouteHistory(User driver) {
        // Usa el método existente y filtra con Java Stream
        List<Route> driverRoutes = routeRepository.findByDriver(driver);
        
        List<Route> completedRoutes = driverRoutes.stream()
                .filter(route -> "COMPLETED".equals(route.getStatus()))
                .sorted((r1, r2) -> {
                    if (r1.getCompletedAt() == null) return 1;
                    if (r2.getCompletedAt() == null) return -1;
                    return r2.getCompletedAt().compareTo(r1.getCompletedAt());
                })
                .collect(Collectors.toList());
        
        return completedRoutes.stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());
    }

    private RouteHistoryDto convertToHistoryDto(Route route) {
        // Calcular tiempo de finalización
        Duration completionDuration = null;
        String formattedTime = "N/A";
        
        if (route.getAssignedAt() != null && route.getCompletedAt() != null) {
            completionDuration = Duration.between(route.getAssignedAt(), route.getCompletedAt());
            long hours = completionDuration.toHours();
            long minutes = completionDuration.toMinutesPart();
            formattedTime = hours + " horas " + minutes + " minutos";
        }
        
        // Calcular velocidad promedio (si hay información suficiente)
        Double averageSpeed = null;
        if (completionDuration != null && route.getDistance() != null && 
                !completionDuration.isZero()) {
            double hours = completionDuration.toMinutes() / 60.0;
            averageSpeed = route.getDistance() / hours;
        }
        
        // Calcular pago basado en la distancia (ejemplo: $10 por km)
        Double calculatedPayment = route.getDistance() != null ? route.getDistance() * 10 : 0.0;
        
        return RouteHistoryDto.builder()
                .routeId(route.getId())
                .origin(route.getOrigin())
                .destination(route.getDestination())
                .distance(route.getDistance())
                .assignedAt(route.getAssignedAt())
                .completedAt(route.getCompletedAt())
                .completionTime(formattedTime)
                .payment(calculatedPayment)
                .status(route.getStatus())
                .averageSpeed(averageSpeed)
                .build();
    }

    public Route cancelRoute(Long routeId, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("This route is not assigned to you");
        }
        
        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("Only assigned routes can be canceled");
        }
        
        // Volver a estado disponible
        route.setStatus("AVAILABLE");
        route.setDriver(null);
        route.setAssignedAt(null);
        
        return routeRepository.save(route);
    }
}