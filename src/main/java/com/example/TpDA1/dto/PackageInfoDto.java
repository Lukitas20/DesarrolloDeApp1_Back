package com.example.TpDA1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageInfoDto {
    private Long packageId;
    private String qrCode;
    private String location;
    private String description;
    private String warehouseSection;
    private String shelfNumber;
    private Double weight;
    private String dimensions;
    private Boolean fragile;
    private LocalDateTime createdAt;
    
    // Información de la ruta asociada
    private Long routeId;
    private String routeOrigin;
    private String routeDestination;
    private Double routeDistance;
    private String routeStatus;
} 