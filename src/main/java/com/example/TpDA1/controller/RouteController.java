package com.example.TpDA1.controller;

import com.example.TpDA1.dto.ReportIncidentDto;
import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Incident;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    // Get available routes for assignment
    @GetMapping("/available")
    public ResponseEntity<List<Route>> getAvailableRoutes() {
        return ResponseEntity.ok(routeService.getAvailableRoutes());
    }

    @GetMapping("/my-routes")
    public ResponseEntity<List<Route>> getMyRoutes(@AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.getDriverRoutes(driver));
    }

    @PostMapping
    public ResponseEntity<Route> createRoute(@RequestBody Route route) {
        if (route.getStatus() == null) {
            route.setStatus("AVAILABLE");
        }
        return ResponseEntity.ok(routeService.createRoute(route));
    }
    @PostMapping("/{routeId}/assign")
    public ResponseEntity<Route> assignRoute(
            @PathVariable("routeId") Long routeId,
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.assignRouteToDriver(routeId, driver));
    }

    @PostMapping("/{routeId}/complete")
    public ResponseEntity<Route> completeRoute(
            @PathVariable("routeId") Long routeId,
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.completeRoute(routeId, driver));
    }

    @PostMapping("/{routeId}/cancel")
    public ResponseEntity<Route> cancelRoute(
            @PathVariable("routeId") Long routeId,
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.cancelRoute(routeId, driver));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RouteHistoryDto>> getRouteHistory(   
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.getDriverRouteHistory(driver));
    }

    @PostMapping(value = "/report-incident", consumes = "multipart/form-data")
    public ResponseEntity<Incident> reportIncident(
            @ModelAttribute ReportIncidentDto dto,
            @AuthenticationPrincipal User driver) {
        Incident incident = routeService.reportIncident(dto, driver);
        return ResponseEntity.ok(incident);
    }
}