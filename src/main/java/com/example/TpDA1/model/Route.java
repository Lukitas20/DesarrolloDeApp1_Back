package com.example.TpDA1.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Getter
@Setter
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private Double distance; // in kilometers

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes

    @Column(nullable = false)
    private String status; // AVAILABLE, ASSIGNED, IN_PROGRESS, COMPLETED

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver; // The assigned driver (if any)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "confirmation_code")
    private String confirmationCode; // Código para completar la entrega

    @OneToOne(mappedBy = "route", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Package packageInfo;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}