package com.example.TpDA1.controller;

import com.example.TpDA1.service.RouteNotificationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final RouteNotificationScheduler routeNotificationScheduler;
    
    // 📡 Endpoint principal para Long Polling
    @GetMapping("/poll")
    public ResponseEntity<RouteNotificationScheduler.NotificationResponse> pollNotifications() {
        try {
            RouteNotificationScheduler.NotificationResponse response = routeNotificationScheduler.getNotificationsForPolling();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error en polling: " + e.getMessage());
            return ResponseEntity.ok(new RouteNotificationScheduler.NotificationResponse(
                false, 
                java.util.List.of(), 
                0, 
                "Error obteniendo notificaciones"
            ));
        }
    }
    
    // ✅ Endpoint para marcar como leídas (mantenido para compatibilidad)
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        // Las notificaciones se marcan automáticamente como leídas al entregarlas
        return ResponseEntity.ok(Map.of("message", "Notificaciones marcadas como leídas"));
    }
} 