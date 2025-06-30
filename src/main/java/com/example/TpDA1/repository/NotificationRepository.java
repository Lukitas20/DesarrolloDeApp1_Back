package com.example.TpDA1.repository;

import com.example.TpDA1.model.Notification;
import com.example.TpDA1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Obtener notificaciones no leídas para un usuario específico
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Obtener notificaciones broadcast no leídas (user = null)
    List<Notification> findByUserIsNullAndIsReadFalseOrderByCreatedAtDesc();
    
    // Obtener todas las notificaciones no leídas para un usuario (específicas + broadcast)
    @Query("SELECT n FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsForUser(@Param("user") User user);
    
    // Obtener notificaciones por tipo
    List<Notification> findByTypeAndIsReadFalseOrderByCreatedAtDesc(String type);
    
    // Obtener notificaciones más recientes que una fecha
    @Query("SELECT n FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsForUserSince(@Param("user") User user, @Param("since") LocalDateTime since);
    
    // Contar notificaciones no leídas para un usuario
    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.user = :user OR n.user IS NULL) AND n.isRead = false")
    Long countUnreadNotificationsForUser(@Param("user") User user);
    
    // Obtener notificaciones por ruta
    List<Notification> findByRouteIdOrderByCreatedAtDesc(Long routeId);
    
    // Marcar notificaciones como leídas por usuario
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE (n.user = :user OR n.user IS NULL) AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user, @Param("readAt") LocalDateTime readAt);
} 