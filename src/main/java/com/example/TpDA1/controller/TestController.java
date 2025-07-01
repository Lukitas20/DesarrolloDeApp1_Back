package com.example.TpDA1.controller;

import com.example.TpDA1.dto.ConfirmDeliveryDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.repository.PackageRepository;
import com.example.TpDA1.service.PackageService;
import com.example.TpDA1.service.QRCodeService;
import com.example.TpDA1.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private QRCodeService qrCodeService;

    // Endpoint para simular el escaneo de QR
    @PostMapping("/scan-qr")
    public ResponseEntity<?> simulateQrScan(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String qrCode = request.get("qrCode");
            String userEmail = authentication.getName();
            
            System.out.println("=== SIMULANDO ESCANEO DE QR ===");
            System.out.println("Usuario: " + userEmail);
            System.out.println("QR Code recibido: " + qrCode);
            
            // Buscar el paquete por QR code (puede ser base64 o string)
            Package packageFound = null;
            
            // Primero intentar buscar por el QR code exacto
            packageFound = packageService.findByQrCode(qrCode);
            
            // Si no se encuentra, buscar paquetes que contengan el QR code en su base64
            if (packageFound == null) {
                List<Package> allPackages = packageService.getAllPackages();
                for (Package pkg : allPackages) {
                    if (pkg.getQrCode() != null && pkg.getQrCode().contains(qrCode)) {
                        packageFound = pkg;
                        break;
                    }
                }
            }
            
            if (packageFound == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "QR code no válido"));
            }
            
            System.out.println("Paquete encontrado: " + packageFound.getId());
            System.out.println("Ruta del paquete: " + packageFound.getRoute().getId());
            System.out.println("Driver de la ruta: " + (packageFound.getRoute().getDriver() != null ? packageFound.getRoute().getDriver().getEmail() : "NULL"));
            
            // Verificar que el paquete esté asignado a una ruta del usuario
            Route route = routeService.findByPackageIdAndUserEmail(packageFound.getId(), userEmail);
            if (route == null) {
                System.out.println("ERROR: No se encontró ruta asignada al usuario " + userEmail + " para el paquete " + packageFound.getId());
                return ResponseEntity.badRequest().body(Map.of("error", "Paquete no asignado a una ruta tuya"));
            }
            
            System.out.println("Ruta encontrada: " + route.getId());
            System.out.println("Estado de ruta: " + route.getStatus());
            
            // Simular el escaneo exitoso - usar el método correcto para rutas asignadas
            String confirmationCode = routeService.scanQrAndActivateRoute(route.getId(), qrCode);
            
            System.out.println("=== ESCANEO EXITOSO ===");
            System.out.println("Código de confirmación generado: " + confirmationCode);
            System.out.println("Ruta cambiada a EN_PROGRESO");
            
            return ResponseEntity.ok(Map.of(
                "message", "QR escaneado exitosamente",
                "confirmationCode", confirmationCode,
                "routeId", route.getId(),
                "packageId", packageFound.getId()
            ));
            
        } catch (Exception e) {
            System.err.println("Error en simulación de QR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint para simular la confirmación de entrega
    @PostMapping("/confirm-delivery")
    public ResponseEntity<?> simulateDeliveryConfirmation(@RequestBody ConfirmDeliveryDto request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            
            System.out.println("=== SIMULANDO CONFIRMACIÓN DE ENTREGA ===");
            System.out.println("Usuario: " + userEmail);
            System.out.println("Ruta ID: " + request.getRouteId());
            System.out.println("Código de confirmación: " + request.getConfirmationCode());
            
            boolean success = routeService.confirmDelivery(request.getRouteId(), request.getConfirmationCode(), userEmail);
            
            if (success) {
                System.out.println("=== ENTREGA CONFIRMADA EXITOSAMENTE ===");
                return ResponseEntity.ok(Map.of("message", "Entrega confirmada exitosamente"));
            } else {
                System.out.println("=== ERROR EN CONFIRMACIÓN ===");
                return ResponseEntity.badRequest().body(Map.of("error", "Código de confirmación inválido"));
            }
            
        } catch (Exception e) {
            System.err.println("Error en confirmación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint para ver todas las rutas del usuario
    @GetMapping("/my-routes")
    public ResponseEntity<?> getMyRoutes(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            List<Route> routes = routeService.getRoutesByUserEmail(userEmail);
            
            System.out.println("=== RUTAS DEL USUARIO ===");
            System.out.println("Usuario: " + userEmail);
            System.out.println("Cantidad de rutas: " + routes.size());
            
            for (Route route : routes) {
                System.out.println("Ruta ID: " + route.getId() + 
                                 " | Estado: " + route.getStatus() + 
                                 " | Paquetes: " + route.getPackages().size());
                
                for (Package pkg : route.getPackages()) {
                    System.out.println("  - Paquete ID: " + pkg.getId() + 
                                     " | QR: " + pkg.getQrCode());
                }
            }
            
            return ResponseEntity.ok(routes);
            
        } catch (Exception e) {
            System.err.println("Error obteniendo rutas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint para crear un paquete de prueba
    @PostMapping("/create-test-package")
    public ResponseEntity<?> createTestPackage(@RequestBody Map<String, String> request) {
        try {
            String description = request.get("description");
            String destination = request.get("destination");
            
            Package newPackage = packageService.createPackage(description, destination);
            
            System.out.println("=== PAQUETE DE PRUEBA CREADO ===");
            System.out.println("ID: " + newPackage.getId());
            System.out.println("QR Code: " + newPackage.getQrCode());
            System.out.println("Descripción: " + newPackage.getDescription());
            
            return ResponseEntity.ok(Map.of(
                "message", "Paquete de prueba creado",
                "packageId", newPackage.getId(),
                "qrCode", newPackage.getQrCode(),
                "description", newPackage.getDescription()
            ));
            
        } catch (Exception e) {
            System.err.println("Error creando paquete: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Endpoint de debug: ver todas las rutas
    @GetMapping("/debug/all-routes")
    public ResponseEntity<?> debugAllRoutes() {
        try {
            List<Route> allRoutes = routeService.getAllRoutesForDebug();
            
            System.out.println("=== DEBUG: TODAS LAS RUTAS ===");
            System.out.println("Total de rutas: " + allRoutes.size());
            
            for (Route route : allRoutes) {
                System.out.println("Ruta ID: " + route.getId() + 
                                 " | Estado: " + route.getStatus() + 
                                 " | Driver: " + (route.getDriver() != null ? route.getDriver().getUsername() : "NULL") +
                                 " | Email: " + (route.getDriver() != null ? route.getDriver().getEmail() : "NULL") +
                                 " | Paquetes: " + (route.getPackages() != null ? route.getPackages().size() : 0));
            }
            
            return ResponseEntity.ok(allRoutes);
            
        } catch (Exception e) {
            System.err.println("Error en debug: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 