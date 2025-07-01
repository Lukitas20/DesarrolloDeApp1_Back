package com.example.TpDA1.controller;

import com.example.TpDA1.model.Package;
import com.example.TpDA1.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/packages")
@RequiredArgsConstructor
public class PackageController {
    
    private final PackageService packageService;
    
    // GET /packages/{packageId} - Obtener paquete
    @GetMapping("/{packageId}")
    public ResponseEntity<Package> getPackage(@PathVariable Long packageId) {
        try {
            Package packageEntity = packageService.getPackageById(packageId);
            return ResponseEntity.ok(packageEntity);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // GET /packages/qr/{qrCode} - Obtener paquete por QR
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Package> getPackageByQr(@PathVariable String qrCode) {
        try {
            Package packageEntity = packageService.getPackageByQrCode(qrCode);
            return ResponseEntity.ok(packageEntity);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // POST /packages/{packageId}/confirm-delivery - Confirmar entrega
    @PostMapping("/{packageId}/confirm-delivery")
    public ResponseEntity<String> confirmDelivery(@PathVariable Long packageId) {
        try {
            packageService.confirmDelivery(packageId);
            return ResponseEntity.ok("Entrega confirmada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al confirmar entrega: " + e.getMessage());
        }
    }
    
    // PUT /packages/{packageId}/status - Actualizar estado
    @PutMapping("/{packageId}/status")
    public ResponseEntity<Package> updatePackageStatus(
            @PathVariable Long packageId, 
            @RequestBody Map<String, String> request) {
        try {
            String newStatus = request.get("status");
            Package updatedPackage = packageService.updatePackageStatus(packageId, newStatus);
            return ResponseEntity.ok(updatedPackage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // GET /packages/health - Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Packages service is healthy");
    }
    
    // POST /packages/validate-qr - Validar QR
    @PostMapping("/validate-qr")
    public ResponseEntity<Map<String, Object>> validateQr(@RequestBody Map<String, String> request) {
        try {
            String qrCode = request.get("qrCode");
            boolean isValid = packageService.validateQrCode(qrCode);
            
            if (isValid) {
                Package packageEntity = packageService.getPackageByQrCode(qrCode);
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "packageId", packageEntity.getId(),
                    "description", packageEntity.getDescription()
                ));
            } else {
                return ResponseEntity.ok(Map.of("valid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("valid", false, "error", e.getMessage()));
        }
    }
} 