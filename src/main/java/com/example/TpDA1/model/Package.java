package com.example.TpDA1.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "packages")
@Data
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String location; // Ubicación en el depósito

    @Column(nullable = false)
    private String description; // Descripción del paquete

    @Column(name = "warehouse_section")
    private String warehouseSection; // Sección del depósito (A, B, C, etc.)

    @Column(name = "shelf_number")
    private String shelfNumber;  // Número de estante (A1, B2, etc.)
    
    @Column(nullable = false)
    private Double weight; // Peso en kg

    @Column(nullable = false)
    private String dimensions; // Dimensiones (ej: "30x20x15 cm")

    @Column(nullable = false)
    private Boolean fragile; // Si es frágil

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;
} 