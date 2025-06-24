package com.example.TpDA1.dto;

import org.springframework.web.multipart.MultipartFile;

public class ReportIncidentDto {
    private String type;
    private String description;
    private MultipartFile photo; // Assume frontend uploads photo as a file
    private Long routeId;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MultipartFile getPhoto() { return photo; }
    public void setPhoto(MultipartFile photo) { this.photo = photo; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
}
