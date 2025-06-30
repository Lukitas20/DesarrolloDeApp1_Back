package com.example.TpDA1.controller;

import com.example.TpDA1.dto.ConfirmationCodeDto;
import com.example.TpDA1.dto.PackageInfoDto;
import com.example.TpDA1.dto.QRScanDto;
import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {
    private final RouteService routeService;

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Routes service is healthy");
    }

    // Get available routes for assignment
    @GetMapping("/available")
    public ResponseEntity<List<Route>> getAvailableRoutes() {
        try {
            System.out.println("🔄 RouteController - Obteniendo rutas disponibles...");
            List<Route> routes = routeService.getAvailableRoutes();
            System.out.println("✅ RouteController - " + routes.size() + " rutas disponibles encontradas");
            
            // Debug: mostrar detalles de las primeras rutas
            for (int i = 0; i < Math.min(3, routes.size()); i++) {
                Route route = routes.get(i);
                System.out.println("🔍 Ruta " + (i+1) + ": ID=" + route.getId() 
                    + ", Origin='" + route.getOrigin() + "'"
                    + ", Destination='" + route.getDestination() + "'"
                    + ", Distance=" + route.getDistance()
                    + ", Status='" + route.getStatus() + "'");
            }
            
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            System.err.println("❌ RouteController - Error al obtener rutas disponibles: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/my-routes")
    public ResponseEntity<List<Route>> getMyRoutes(@AuthenticationPrincipal User driver) {
        try {
            System.out.println("🔄 RouteController - Obteniendo rutas del conductor: " + driver.getUsername());
            List<Route> routes = routeService.getDriverRoutes(driver);
            System.out.println("✅ RouteController - " + routes.size() + " rutas encontradas para el conductor");
            return ResponseEntity.ok(routes);
        } catch (Exception e) {
            System.err.println("❌ RouteController - Error al obtener rutas del conductor: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        try {
            System.out.println("🎯 RouteController - Solicitud de asignación de ruta " + routeId + " por " + driver.getUsername());
            Route assignedRoute = routeService.assignRoute(routeId, driver);
            return ResponseEntity.ok(assignedRoute);
        } catch (Exception e) {
            System.err.println("❌ RouteController - Error asignando ruta: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{routeId}/complete")
    public ResponseEntity<Route> completeRoute(
            @PathVariable("routeId") Long routeId,
            @AuthenticationPrincipal User driver) {
        try {
            System.out.println("✅ RouteController - Solicitud de completar ruta " + routeId + " por " + driver.getUsername());
            Route completedRoute = routeService.completeRoute(routeId, driver);
            return ResponseEntity.ok(completedRoute);
        } catch (Exception e) {
            System.err.println("❌ RouteController - Error completando ruta: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{routeId}/cancel")
    public ResponseEntity<Route> cancelRoute(
            @PathVariable("routeId") Long routeId,
            @AuthenticationPrincipal User driver) {
        try {
            System.out.println("🚫 RouteController - Solicitud de cancelar ruta " + routeId + " por " + driver.getUsername());
            Route cancelledRoute = routeService.cancelRoute(routeId, driver);
            return ResponseEntity.ok(cancelledRoute);
        } catch (Exception e) {
            System.err.println("❌ RouteController - Error cancelando ruta: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<RouteHistoryDto>> getRouteHistory(   
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.getDriverRouteHistory(driver));
    }

    @PostMapping("/scan-qr")
    public ResponseEntity<PackageInfoDto> scanQRCode(
            @RequestBody QRScanDto qrScanDto,
            @AuthenticationPrincipal User driver) {
        try {
            PackageInfoDto packageInfo = routeService.scanQRAndUnlockRoute(qrScanDto.getQrCode(), driver);
            return ResponseEntity.ok(packageInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{routeId}/complete-with-code")
    public ResponseEntity<Route> completeWithConfirmationCode(
            @PathVariable("routeId") Long routeId,
            @RequestBody ConfirmationCodeDto confirmationCodeDto,
            @AuthenticationPrincipal User driver) {
        try {
            Route completedRoute = routeService.completeRouteWithCode(
                    routeId, 
                    confirmationCodeDto.getConfirmationCode(), 
                    driver
            );
            return ResponseEntity.ok(completedRoute);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}