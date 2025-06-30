package com.example.TpDA1.service;

import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.RouteRepository;
import com.example.TpDA1.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    public Route updateRoute(Route route) {
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

    // Método para buscar ruta por paquete ID y email de usuario
    public Route findByPackageIdAndUserEmail(Long packageId, String userEmail) {
        // Primero buscar el usuario por username para obtener su email real
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));
        
        // Luego buscar ruta por el email real del usuario
        return routeRepository.findByPackagesIdAndDriverEmail(packageId, user.getEmail())
                .orElse(null);
    }

    // Método para escanear QR y activar ruta
    public String scanQrAndActivateRoute(Long routeId, String qrCode) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("Route must be assigned before scanning QR");
        }
        
        // Cambiar estado a EN_PROGRESO
        route.setStatus("EN_PROGRESO");
        route.setStartedAt(LocalDateTime.now());
        
        // Generar código de confirmación
        String confirmationCode = generateConfirmationCode();
        route.setConfirmationCode(confirmationCode);
        
        routeRepository.save(route);
        
        System.out.println("🔓 Ruta activada: " + routeId);
        System.out.println("📱 Código de confirmación: " + confirmationCode);
        
        return confirmationCode;
    }

    // Método para confirmar entrega
    public boolean confirmDelivery(Long routeId, String confirmationCode, String userEmail) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        // Primero buscar el usuario por username para obtener su email real
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));
        
        if (route.getDriver() == null || !route.getDriver().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Route is not assigned to this user");
        }
        
        if (!"EN_PROGRESO".equals(route.getStatus())) {
            throw new RuntimeException("Route must be in progress to confirm delivery");
        }
        
        if (!confirmationCode.equals(route.getConfirmationCode())) {
            return false; // Código incorrecto
        }
        
        // Completar la ruta
        route.setStatus("COMPLETED");
        route.setCompletedAt(LocalDateTime.now());
        routeRepository.save(route);
        
        System.out.println("✅ Entrega confirmada: " + routeId);
        return true;
    }

    // Método para obtener rutas por email de usuario
    public List<Route> getRoutesByUserEmail(String userEmail) {
        // Primero buscar el usuario por username para obtener su email real
        User user = userRepository.findByUsername(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userEmail));
        
        // Luego buscar rutas por el email real del usuario
        return routeRepository.findByDriverEmail(user.getEmail());
    }

    // Método para generar código de confirmación
    private String generateConfirmationCode() {
        // Generar código de 6 dígitos
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    // Método de debug: obtener todas las rutas con drivers y paquetes
    public List<Route> getAllRoutesForDebug() {
        return routeRepository.findAllWithDriverAndPackages();
    }
}