package com.example.TpDA1.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Getter
@Setter
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    private String qrCode;

    @Column(nullable = false)
    private String location; // ubicación específica en depósito

    @Column(nullable = false)
    private String description;

    @Column(name = "warehouse_section")
    private String warehouseSection; // sección del depósito (A, B, C, etc.)

    @Column(name = "shelf_number")
    private String shelfNumber; // número de estante

    private Double weight; // peso en kg

    private String dimensions; // dimensiones (ej: "30x20x15 cm")

    @Column(name = "fragile")
    private Boolean fragile = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 