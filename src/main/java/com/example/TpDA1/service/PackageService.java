package com.example.TpDA1.service;

import com.example.TpDA1.dto.CreatePackageDto;
import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.repository.PackageRepository;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageService {
    
    private final PackageRepository packageRepository;
    private final RouteRepository routeRepository;
    private final QRCodeService qrCodeService;

    @Transactional
    public Package createPackageWithRoute(CreatePackageDto dto) {
        // 1. Crear la ruta
        Route route = new Route();
        route.setOrigin(dto.getRouteOrigin());
        route.setDestination(dto.getRouteDestination());
        route.setDistance(dto.getRouteDistance());
        route.setEstimatedDuration(dto.getEstimatedDuration());
        route.setStatus("AVAILABLE");
        
        Route savedRoute = routeRepository.save(route);

        // 2. Crear el paquete
        Package packageEntity = new Package();
        packageEntity.setLocation(dto.getLocation());
        packageEntity.setDescription(dto.getDescription());
        packageEntity.setWarehouseSection(dto.getWarehouseSection());
        packageEntity.setShelfNumber(dto.getShelfNumber());
        packageEntity.setWeight(dto.getWeight());
        packageEntity.setDimensions(dto.getDimensions());
        packageEntity.setFragile(dto.getFragile() != null ? dto.getFragile() : false);
        packageEntity.setRoute(savedRoute);

        // 3. Generar QR automáticamente ANTES de guardar
        String qrCode = qrCodeService.generateQRCodeForPackage("TEMP_" + System.currentTimeMillis());
        packageEntity.setQrCode(qrCode);
        
        // 4. Guardar el paquete con QR
        Package savedPackage = packageRepository.save(packageEntity);

        // 5. Generar QR final con el ID real
        String finalQrCode = qrCodeService.generateQRCodeForPackage(savedPackage.getId().toString());
        savedPackage.setQrCode(finalQrCode);
        
        // 6. Actualizar el paquete con el QR final
        savedPackage = packageRepository.save(savedPackage);

        // 7. Mostrar información en logs
        System.out.println("📦 PAQUETE CREADO EXITOSAMENTE");
        System.out.println("ID del Paquete: " + savedPackage.getId());
        System.out.println("ID de la Ruta: " + savedRoute.getId());
        System.out.println("Ubicación: " + savedPackage.getLocation());
        System.out.println("Sección: " + savedPackage.getWarehouseSection());
        System.out.println("Estante: " + savedPackage.getShelfNumber());
        System.out.println("Destino: " + savedRoute.getDestination());
        System.out.println("🔑 CÓDIGO QR GENERADO:");
        System.out.println("Datos del QR: PACKAGE_" + savedPackage.getId());
        System.out.println("QR Code: " + finalQrCode);
        System.out.println("==========================================");

        return savedPackage;
    }

    public List<Package> getAllPackages() {
        return packageRepository.findAll();
    }

    public long getPackageCount() {
        return packageRepository.count();
    }

    @Transactional
    public Package updatePackage(Package packageEntity) {
        return packageRepository.save(packageEntity);
    }
} 