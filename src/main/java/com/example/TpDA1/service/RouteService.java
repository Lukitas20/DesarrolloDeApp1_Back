package com.example.TpDA1.service;

import com.example.TpDA1.dto.PackageInfoDto;
import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.PackageRepository;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final PackageRepository packageRepository;

    // Get all available routes (not assigned to any driver)
    public List<Route> getAvailableRoutes() {
        return routeRepository.findByStatus("AVAILABLE");
    }

    // Get routes assigned to a specific driver
    public List<Route> getDriverRoutes(User driver) {
        return routeRepository.findByDriver(driver);
    }

    // Assign a route to a driver
    @Transactional(timeout = 30)
    public Route assignRouteToDriver(Long routeId, User driver) {
        try {
            System.out.println("🔄 RouteService - Iniciando asignación de ruta " + routeId + " para: " + driver.getUsername());
            
            // Buscar la ruta con bloqueo pesimista para evitar race conditions
            Route route = routeRepository.findById(routeId)
                    .orElseThrow(() -> {
                        System.err.println("❌ RouteService - Ruta no encontrada: " + routeId);
                        return new RuntimeException("Route not found with ID: " + routeId);
                    });

            System.out.println("🔍 RouteService - Ruta encontrada. Estado actual: " + route.getStatus());
            
            if (!"AVAILABLE".equals(route.getStatus())) {
                System.err.println("❌ RouteService - Ruta no disponible. Estado: " + route.getStatus());
                throw new RuntimeException("Route is not available for assignment. Current status: " + route.getStatus());
            }

            // Verificar que el conductor no esté ya asignado
            if (route.getDriver() != null) {
                System.err.println("❌ RouteService - Ruta ya asignada a otro conductor: " + route.getDriver().getUsername());
                throw new RuntimeException("Route is already assigned to another driver");
            }

            System.out.println("🔄 RouteService - Asignando ruta a conductor...");
            route.setDriver(driver);
            route.setStatus("ASSIGNED");
            route.setAssignedAt(LocalDateTime.now());
            
            // Generar código de confirmación cuando se asigna la ruta
            if (route.getConfirmationCode() == null) {
                route.setConfirmationCode(generateConfirmationCode());
                System.out.println("🔑 RouteService - Código de confirmación generado");
            }

            Route savedRoute = routeRepository.save(route);
            System.out.println("✅ RouteService - Ruta asignada exitosamente. ID: " + savedRoute.getId() + ", Estado: " + savedRoute.getStatus());
            
            return savedRoute;
            
        } catch (Exception e) {
            System.err.println("❌ RouteService - Error en assignRouteToDriver: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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

    // Escanear código QR y desbloquear ruta
    @Transactional(timeout = 30)
    public PackageInfoDto scanQRAndUnlockRoute(String qrCode, User driver) {
        // Buscar el paquete por código QR
        Package packageEntity = packageRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new RuntimeException("Código QR no válido"));

        Route route = packageEntity.getRoute();
        if (route == null) {
            throw new RuntimeException("No hay ruta asociada a este paquete");
        }

        if (!"AVAILABLE".equals(route.getStatus())) {
            throw new RuntimeException("Esta ruta no está disponible");
        }

        // Mostrar información del paquete por consola (simulación)
        System.out.println("=== INFORMACIÓN DEL PAQUETE ===");
        System.out.println("Código QR: " + packageEntity.getQrCode());
        System.out.println("Ubicación en depósito: " + packageEntity.getLocation());
        System.out.println("Sección: " + packageEntity.getWarehouseSection());
        System.out.println("Estante: " + packageEntity.getShelfNumber());
        System.out.println("Descripción: " + packageEntity.getDescription());
        System.out.println("Peso: " + packageEntity.getWeight() + " kg");
        System.out.println("Dimensiones: " + packageEntity.getDimensions());
        System.out.println("Frágil: " + (packageEntity.getFragile() ? "Sí" : "No"));
        System.out.println("=== RUTA ASOCIADA ===");
        System.out.println("Origen: " + route.getOrigin());
        System.out.println("Destino: " + route.getDestination());
        System.out.println("Distancia: " + route.getDistance() + " km");
        System.out.println("================================");

        // Asignar automáticamente la ruta al conductor
        assignRouteToDriver(route.getId(), driver);

        // Retornar información del paquete
        return PackageInfoDto.builder()
                .packageId(packageEntity.getId())
                .qrCode(packageEntity.getQrCode())
                .location(packageEntity.getLocation())
                .description(packageEntity.getDescription())
                .warehouseSection(packageEntity.getWarehouseSection())
                .shelfNumber(packageEntity.getShelfNumber())
                .weight(packageEntity.getWeight())
                .dimensions(packageEntity.getDimensions())
                .fragile(packageEntity.getFragile())
                .createdAt(packageEntity.getCreatedAt())
                .routeId(route.getId())
                .routeOrigin(route.getOrigin())
                .routeDestination(route.getDestination())
                .routeDistance(route.getDistance())
                .routeStatus(route.getStatus())
                .build();
    }

    // Completar ruta con código de confirmación
    @Transactional(timeout = 30)
    public Route completeRouteWithCode(Long routeId, String confirmationCode, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Esta ruta no está asignada a usted");
        }

        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("Solo se pueden completar rutas asignadas");
        }

        if (!confirmationCode.equals(route.getConfirmationCode())) {
            throw new RuntimeException("Código de confirmación incorrecto");
        }

        // Mostrar código de confirmación por consola (simulación del comprador)
        System.out.println("=== CÓDIGO DE CONFIRMACIÓN ===");
        System.out.println("El comprador proporciona el código: " + confirmationCode);
        System.out.println("✅ Código verificado correctamente");
        System.out.println("🎉 Entrega completada exitosamente");
        System.out.println("==============================");

        route.setStatus("COMPLETED");
        route.setCompletedAt(LocalDateTime.now());

        return routeRepository.save(route);
    }

    // Generar código de confirmación
    private String generateConfirmationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // Código de 6 dígitos
        return String.valueOf(code);
    }
}