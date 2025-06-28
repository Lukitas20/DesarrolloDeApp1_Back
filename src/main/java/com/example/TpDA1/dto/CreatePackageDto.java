package com.example.TpDA1.dto;

import lombok.Data;

@Data
public class CreatePackageDto {
    private String location;
    private String description;
    private String warehouseSection;
    private String shelfNumber;
    private Double weight;
    private String dimensions;
    private Boolean fragile;
    private String routeOrigin;
    private String routeDestination;
    private Double distance;
    private Integer estimatedDuration;
    private Double routeDistance;
} 