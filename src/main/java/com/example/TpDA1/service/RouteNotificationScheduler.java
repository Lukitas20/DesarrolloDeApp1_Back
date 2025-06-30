package com.example.TpDA1.service;

import com.example.TpDA1.model.Route;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class RouteNotificationScheduler {

    private final RouteRepository routeRepository;
    
    // Cache en memoria para notificaciones activas
    private final ConcurrentMap<String, NotificationData> activeNotifications = new ConcurrentHashMap<>();
    
    // Clase interna para datos de notificación
    private static class NotificationData {
        public String title;
        public String message;
        public String type;
        public LocalDateTime createdAt;
        
        public NotificationData(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Ejecutar cada 2 minutos - notificar sobre rutas disponibles
    @Scheduled(fixedDelay = 120000) // 2 minutos
    public void notifyAvailableRoutes() {
        try {
            List<Route> availableRoutes = routeRepository.findByStatus("AVAILABLE");
            int currentCount = availableRoutes.size();
            
            String title = generateNotificationTitle(currentCount);
            String message = generateNotificationMessage(currentCount, availableRoutes);
            
            // Guardar en cache de memoria con timestamp único
            String notificationId = "routes_update_" + System.currentTimeMillis();
            activeNotifications.put(notificationId, new NotificationData(title, message, "routes_status_update"));
            
            System.out.println("📅 [" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + title);
            
            // Limpiar notificaciones antiguas (más de 5 minutos)
            cleanupOldNotifications();
            
        } catch (Exception e) {
            System.err.println("❌ Error en notificación programada: " + e.getMessage());
        }
    }
    
    // Generar título de notificación
    private String generateNotificationTitle(int count) {
        if (count == 0) {
            return "📭 No hay rutas disponibles";
        } else if (count == 1) {
            return "🚚 1 Ruta Disponible";
        } else {
            return "🚚 " + count + " Rutas Disponibles";
        }
    }
    
    // Generar mensaje de notificación
    private String generateNotificationMessage(int count, List<Route> routes) {
        if (count == 0) {
            return "No hay rutas disponibles en este momento.";
        }
        
        if (count == 1) {
            Route route = routes.get(0);
            return String.format("Ruta: %s → %s (%.1f km, $%.0f)", 
                route.getOrigin(), 
                route.getDestination(), 
                route.getDistance(),
                route.getDistance() * 50
            );
        }
        
        // Para múltiples rutas, mostrar la primera como ejemplo
        Route exampleRoute = routes.get(0);
        return String.format("%d rutas disponibles. Ej: %s → %s (%.1f km, $%.0f)", 
            count,
            exampleRoute.getOrigin(), 
            exampleRoute.getDestination(), 
            exampleRoute.getDistance(),
            exampleRoute.getDistance() * 50
        );
    }
    
    // Limpiar notificaciones antiguas del cache
    private void cleanupOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        activeNotifications.entrySet().removeIf(entry -> 
            entry.getValue().createdAt.isBefore(cutoff)
        );
    }
    
    // Obtener notificaciones para Long Polling
    public NotificationResponse getNotificationsForPolling() {
        try {
            if (activeNotifications.isEmpty()) {
                return new NotificationResponse(true, List.of(), 0, "Sin notificaciones nuevas");
            }
            
            // Convertir notificaciones de memoria a formato para frontend
            List<NotificationDto> notifications = activeNotifications.entrySet().stream()
                .map(entry -> new NotificationDto(
                    entry.getKey(),
                    entry.getValue().title,
                    entry.getValue().message,
                    entry.getValue().type,
                    entry.getValue().createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ))
                .toList();
            
            // Limpiar después de entregar
            activeNotifications.clear();
            
            return new NotificationResponse(true, notifications, notifications.size(), "Notificaciones entregadas");
            
        } catch (Exception e) {
            System.err.println("❌ Error obteniendo notificaciones: " + e.getMessage());
            return new NotificationResponse(false, List.of(), 0, "Error obteniendo notificaciones");
        }
    }
    
    // Crear notificación inmediata de ruta asignada
    public void notifyRouteAssigned(Route route) {
        try {
            String title = "✅ Ruta Asignada";
            String message = String.format("Ruta asignada: %s → %s (%.1f km, $%.0f)", 
                route.getOrigin(), 
                route.getDestination(), 
                route.getDistance(),
                route.getDistance() * 50
            );
            
            String notificationId = "route_assigned_" + route.getId() + "_" + System.currentTimeMillis();
            activeNotifications.put(notificationId, new NotificationData(title, message, "route_assigned"));
            
        } catch (Exception e) {
            System.err.println("❌ Error creando notificación de asignación: " + e.getMessage());
        }
    }
    
    // Crear notificación inmediata de ruta cancelada
    public void notifyRouteCancelled(Route route) {
        try {
            String title = "❌ Ruta Cancelada";
            String message = String.format("Ruta cancelada: %s → %s (%.1f km)", 
                route.getOrigin(), 
                route.getDestination(), 
                route.getDistance()
            );
            
            String notificationId = "route_cancelled_" + route.getId() + "_" + System.currentTimeMillis();
            activeNotifications.put(notificationId, new NotificationData(title, message, "route_cancelled"));
            
        } catch (Exception e) {
            System.err.println("❌ Error creando notificación de cancelación: " + e.getMessage());
        }
    }
    
    // Crear notificación inmediata de ruta completada
    public void notifyRouteCompleted(Route route) {
        try {
            String title = "🎉 Ruta Completada";
            String message = String.format("¡Ruta completada! %s → %s (%.1f km, $%.0f ganados)", 
                route.getOrigin(), 
                route.getDestination(), 
                route.getDistance(),
                route.getDistance() * 50
            );
            
            String notificationId = "route_completed_" + route.getId() + "_" + System.currentTimeMillis();
            activeNotifications.put(notificationId, new NotificationData(title, message, "route_completed"));
            
        } catch (Exception e) {
            System.err.println("❌ Error creando notificación de completación: " + e.getMessage());
        }
    }
    
    // DTOs para respuestas
    public static class NotificationResponse {
        public boolean success;
        public List<NotificationDto> notifications;
        public int unreadCount;
        public String message;
        
        public NotificationResponse(boolean success, List<NotificationDto> notifications, int unreadCount, String message) {
            this.success = success;
            this.notifications = notifications;
            this.unreadCount = unreadCount;
            this.message = message;
        }
    }
    
    public static class NotificationDto {
        public String id;
        public String title;
        public String message;
        public String type;
        public String createdAt;
        
        public NotificationDto(String id, String title, String message, String type, String createdAt) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.type = type;
            this.createdAt = createdAt;
        }
    }
} 