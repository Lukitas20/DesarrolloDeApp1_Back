package com.example.TpDA1.dto;

public class ReportIncidentDto {
    private String type;
    private String description;
    private String photoUrl; // Assume frontend uploads photo and provides URL
    private Long routeId;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
}
