package com.example.TpDA1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    private String type; // "new_route", "route_assigned", "route_completed", etc.
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user; // Usuario al que va dirigida la notificación (null = broadcast)
    
    @ManyToOne
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route; // Ruta relacionada (opcional)
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "data", columnDefinition = "TEXT")
    private String data; // JSON con datos adicionales
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructor vacío
    public Notification() {}
    
    // Constructor para notificaciones específicas de usuario
    public Notification(String title, String message, String type, User user) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.user = user;
        this.isRead = false;
    }
    
    // Constructor para notificaciones broadcast
    public Notification(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.user = null; // Broadcast
        this.isRead = false;
    }
    
    // Constructor completo
    public Notification(String title, String message, String type, User user, Route route, String data) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.user = user;
        this.route = route;
        this.data = data;
        this.isRead = false;
    }
} 