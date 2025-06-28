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
        route.setDistance(dto.getDistance());
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
        packageEntity.setQrCode("TEMP_QR"); // QR temporal

        // 3. Guardar el paquete
        Package savedPackage = packageRepository.save(packageEntity);

        // 4. Generar QR final con Base64
        String finalQrCode = qrCodeService.generateQRCodeForPackage(savedPackage);
        savedPackage.setQrCode(finalQrCode);
        
        // 5. Actualizar el paquete
        savedPackage = packageRepository.save(savedPackage);

        System.out.println("✅ Paquete creado: " + savedPackage.getId() + " con QR: " + finalQrCode);

        return savedPackage;
    }

    @Transactional(readOnly = true)
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

    @Transactional
    public Package createPackage(Package packageEntity) {
        return packageRepository.save(packageEntity);
    }

    @Transactional(readOnly = true)
    public Package getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found"));
    }

    @Transactional(readOnly = true)
    public List<Package> getPackagesByRouteId(Long routeId) {
        return packageRepository.findByRouteId(routeId);
    }

    @Transactional(readOnly = true)
    public Package getPackageByQrCode(String qrCode) {
        List<Package> packages = packageRepository.findByQrCode(qrCode);
        if (packages.isEmpty()) {
            throw new RuntimeException("Package not found with QR code: " + qrCode);
        }
        return packages.get(0);
    }
} 