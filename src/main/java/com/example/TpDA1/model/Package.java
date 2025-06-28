package com.example.TpDA1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    @Column(name = "qr_code", nullable = false, unique = true, columnDefinition = "TEXT")
    private String qrCode;

    @Column(nullable = false)
    private String location; // Ubicación en el depósito

    @Column(nullable = false)
    private String description; // Descripción del paquete

    @Column(name = "warehouse_section")
    private String warehouseSection; // Sección del depósito (A, B, C, etc.)

    @Column(name = "shelf_number")
    private String shelfNumber; // Número de estante (A1, B2, etc.)

    @Column
    private Double weight; // Peso en kg

    @Column
    private String dimensions; // Dimensiones (ej: "30x20x15 cm")

    @Column
    private Boolean fragile = false; // Si es frágil

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 