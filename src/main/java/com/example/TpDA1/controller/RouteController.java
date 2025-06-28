package com.example.TpDA1.controller;

import com.example.TpDA1.dto.CreatePackageDto;
import com.example.TpDA1.dto.RouteHistoryDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.service.PackageService;
import com.example.TpDA1.service.QRCodeService;
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
    private final PackageService packageService;
    private final QRCodeService qrCodeService;

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

    // Crear paquetes de prueba con QR
    @PostMapping("/create-test-packages")
    public ResponseEntity<String> createTestPackages() {
        try {
            // Paquete 1
            CreatePackageDto package1 = new CreatePackageDto();
            package1.setLocation("Depósito Central - Sección A");
            package1.setDescription("Paquete de ropa deportiva");
            package1.setWarehouseSection("A");
            package1.setShelfNumber("A-15");
            package1.setWeight(2.5);
            package1.setDimensions("30x20x15 cm");
            package1.setFragile(false);
            package1.setRouteOrigin("Depósito Central");
            package1.setRouteDestination("Av. Corrientes 1234, CABA");
            package1.setDistance(15.5);
            package1.setEstimatedDuration(45);
            package1.setRouteDistance(15.5);
            
            Package savedPackage1 = packageService.createPackageWithRoute(package1);
            System.out.println("✅ Paquete 1 creado: " + savedPackage1.getId());

            // Paquete 2
            CreatePackageDto package2 = new CreatePackageDto();
            package2.setLocation("Depósito Central - Sección B");
            package2.setDescription("Electrónicos - Smartphone");
            package2.setWarehouseSection("B");
            package2.setShelfNumber("B-22");
            package2.setWeight(0.8);
            package2.setDimensions("15x8x2 cm");
            package2.setFragile(true);
            package2.setRouteOrigin("Depósito Central");
            package2.setRouteDestination("Belgrano 567, CABA");
            package2.setDistance(8.2);
            package2.setEstimatedDuration(25);
            package2.setRouteDistance(8.2);
            
            Package savedPackage2 = packageService.createPackageWithRoute(package2);
            System.out.println("✅ Paquete 2 creado: " + savedPackage2.getId());

            // Paquete 3
            CreatePackageDto package3 = new CreatePackageDto();
            package3.setLocation("Depósito Central - Sección C");
            package3.setDescription("Libros y documentos");
            package3.setWarehouseSection("C");
            package3.setShelfNumber("C-08");
            package3.setWeight(1.2);
            package3.setDimensions("25x18x5 cm");
            package3.setFragile(false);
            package3.setRouteOrigin("Depósito Central");
            package3.setRouteDestination("Palermo 890, CABA");
            package3.setDistance(12.8);
            package3.setEstimatedDuration(35);
            package3.setRouteDistance(12.8);
            
            Package savedPackage3 = packageService.createPackageWithRoute(package3);
            System.out.println("✅ Paquete 3 creado: " + savedPackage3.getId());

            return ResponseEntity.ok("✅ 3 paquetes de prueba creados exitosamente con QR codes!");
            
        } catch (Exception e) {
            System.err.println("❌ Error al crear paquetes de prueba: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error al crear paquetes de prueba: " + e.getMessage());
        }
    }

    // Confirmar entrega escaneando QR
    @PostMapping("/confirm-delivery")
    public ResponseEntity<String> confirmDelivery(@RequestParam String qrCode, @AuthenticationPrincipal User driver) {
        try {
            // Buscar el paquete por QR code
            Package packageEntity = packageService.getPackageByQrCode(qrCode);
            
            // Verificar que el paquete pertenece a una ruta del driver
            Route route = packageEntity.getRoute();
            if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
                return ResponseEntity.badRequest().body("❌ Este paquete no pertenece a tus rutas asignadas");
            }
            
            // Cambiar estado de la ruta a EN_PROGRESO
            if ("ASSIGNED".equals(route.getStatus())) {
                route.setStatus("IN_PROGRESS");
                routeService.updateRoute(route);
                return ResponseEntity.ok("✅ Entrega confirmada! Ruta cambiada a EN_PROGRESO");
            } else {
                return ResponseEntity.badRequest().body("❌ La ruta no está en estado ASIGNADO");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error confirmando entrega: " + e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
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
    public ResponseEntity<List<RouteHistoryDto>> getRouteHistory(   
            @AuthenticationPrincipal User driver) {
        return ResponseEntity.ok(routeService.getDriverRouteHistory(driver));
    }
}