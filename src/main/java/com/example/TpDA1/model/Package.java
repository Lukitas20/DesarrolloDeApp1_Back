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
    private String location;
    
    @Column(nullable = false)
    private String description;
    
    @Column(name = "warehouse_section")
    private String warehouseSection;
    
    @Column(name = "shelf_number")
    private String shelfNumber;
    
    @Column(nullable = false)
    private Double weight;
    
    @Column(nullable = false)
    private String dimensions;
    
    @Column(nullable = false)
    private Boolean fragile;
    
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