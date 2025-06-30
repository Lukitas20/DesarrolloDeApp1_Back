package com.example.TpDA1.service;

import com.example.TpDA1.dto.PackageInfoDto;
import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.PackageRepository;
import com.example.TpDA1.repository.RouteRepository;
import com.example.TpDA1.repository.UserRepository;
import com.example.TpDA1.service.RouteNotificationScheduler;
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
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final RouteNotificationScheduler routeNotificationScheduler;

    // Get all available routes (not assigned to any driver)
    public List<Route> getAvailableRoutes() {
        return routeRepository.findByStatus("AVAILABLE");
    }

    // Get routes assigned to a specific driver
    public List<Route> getDriverRoutes(User driver) {
        return routeRepository.findByDriver(driver);
    }

    // Assign a route to a driver
    @Transactional
    public Route assignRoute(Long routeId, User driver) {
        try {
            Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

            if (!"AVAILABLE".equals(route.getStatus())) {
                throw new RuntimeException("La ruta no está disponible");
            }

            route.setDriver(driver);
            route.setStatus("ASSIGNED");
            route.setAssignedAt(LocalDateTime.now());

            Route savedRoute = routeRepository.save(route);
            System.out.println("✅ Ruta " + routeId + " asignada a " + driver.getUsername());
            
            // Notificación inmediata
            try {
                routeNotificationScheduler.notifyRouteAssigned(savedRoute);
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando notificación de asignación: " + e.getMessage());
            }
            
            return savedRoute;
        } catch (Exception e) {
            System.err.println("❌ Error asignando ruta: " + e.getMessage());
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
        Route savedRoute = routeRepository.save(route);
        
        // Sin notificaciones al crear rutas - solo el scheduler each 2 minutos
        
        return savedRoute;
    }

    public Route updateRoute(Route route) {
        return routeRepository.save(route);
    }
  
    @Transactional
    public Route completeRoute(Long routeId, User driver) {
        try {
            Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

            if (!route.getDriver().getId().equals(driver.getId())) {
                throw new RuntimeException("No puedes completar una ruta que no es tuya");
            }

            if (!"ASSIGNED".equals(route.getStatus()) && !"IN_PROGRESS".equals(route.getStatus())) {
                throw new RuntimeException("La ruta debe estar asignada o en progreso para completarla");
            }

            route.setStatus("COMPLETED");
            route.setCompletedAt(LocalDateTime.now());

            Route savedRoute = routeRepository.save(route);
            System.out.println("✅ Ruta " + routeId + " completada por " + driver.getUsername());
            
            // Notificación inmediata
            try {
                routeNotificationScheduler.notifyRouteCompleted(savedRoute);
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando notificación de completación: " + e.getMessage());
            }
            
            return savedRoute;
        } catch (Exception e) {
            System.err.println("❌ Error completando ruta: " + e.getMessage());
            throw e;
        }
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

    @Transactional
    public Route cancelRoute(Long routeId, User driver) {
        try {
            Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

            if (!route.getDriver().getId().equals(driver.getId())) {
                throw new RuntimeException("No puedes cancelar una ruta que no es tuya");
            }

            if ("COMPLETED".equals(route.getStatus())) {
                throw new RuntimeException("No puedes cancelar una ruta completada");
            }

            route.setDriver(null);
            route.setStatus("AVAILABLE");
            route.setAssignedAt(null);

            Route savedRoute = routeRepository.save(route);
            System.out.println("❌ Ruta " + routeId + " cancelada por " + driver.getUsername());
            
            // Notificación inmediata
            try {
                routeNotificationScheduler.notifyRouteCancelled(savedRoute);
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando notificación de cancelación: " + e.getMessage());
            }
            
            return savedRoute;
        } catch (Exception e) {
            System.err.println("❌ Error cancelando ruta: " + e.getMessage());
            throw e;
        }
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
        assignRoute(route.getId(), driver);

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

        Route savedRoute = routeRepository.save(route);

        // Notificación inmediata
        try {
            routeNotificationScheduler.notifyRouteCompleted(savedRoute);
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando notificación de completación: " + e.getMessage());
        }

        return savedRoute;
    }

    // Generar código de confirmación
    private String generateConfirmationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // Código de 6 dígitos
        return String.valueOf(code);
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