package com.example.TpDA1.controller;

import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.service.PackageService;
import com.example.TpDA1.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;
    private final PackageService packageService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backend is running! 🚀");
    }

    // Get all packages
    @GetMapping("/packages")
    public ResponseEntity<List<Package>> getAllPackages() {
        return ResponseEntity.ok(packageService.getAllPackages());
    }

    // Confirmar entrega escaneando QR
    @PostMapping("/confirm-delivery")
    public ResponseEntity<String> confirmDelivery(@RequestParam String qrCode, @AuthenticationPrincipal User driver) {
        try {
            Package packageEntity = packageService.getPackageByQrCode(qrCode);
            Route route = packageEntity.getRoute();
            
            if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
                return ResponseEntity.badRequest().body("Este paquete no pertenece a tus rutas asignadas");
            }
            
            if ("ASSIGNED".equals(route.getStatus())) {
                route.setStatus("IN_PROGRESS");
                routeService.updateRoute(route);
                return ResponseEntity.ok("Entrega confirmada! Ruta cambiada a EN_PROGRESO");
            } else {
                return ResponseEntity.badRequest().body("La ruta no está en estado ASIGNADO");
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

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
    public ResponseEntity<List<RouteHistoryDto>> getRouteHistory(@AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.getDriverRouteHistory(driver));
    }
}