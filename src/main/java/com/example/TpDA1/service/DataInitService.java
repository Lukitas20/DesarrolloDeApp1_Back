package com.example.TpDA1.service;

import com.example.TpDA1.model.Package;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.repository.PackageRepository;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DataInitService implements CommandLineRunner {
    
    private final RouteRepository routeRepository;
    private final PackageRepository packageRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo ejecutar si no hay datos existentes
        if (routeRepository.count() == 0) {
            initializeTestData();
        }
    }

    private void initializeTestData() {
        // Crear rutas de prueba
        Route route1 = new Route();
        route1.setOrigin("Depósito Central - Av. Libertador 1234");
        route1.setDestination("Zona Norte - Av. Santa Fe 5678");
        route1.setDistance(12.5);
        route1.setEstimatedDuration(35);
        route1.setStatus("AVAILABLE");
        route1 = routeRepository.save(route1);

        Route route2 = new Route();
        route2.setOrigin("Depósito Central - Av. Libertador 1234");
        route2.setDestination("Zona Sur - Av. Rivadavia 9876");
        route2.setDistance(18.2);
        route2.setEstimatedDuration(45);
        route2.setStatus("AVAILABLE");
        route2 = routeRepository.save(route2);

        Route route3 = new Route();
        route3.setOrigin("Depósito Central - Av. Libertador 1234");
        route3.setDestination("Zona Oeste - Av. Corrientes 4321");
        route3.setDistance(8.7);
        route3.setEstimatedDuration(25);
        route3.setStatus("AVAILABLE");
        route3 = routeRepository.save(route3);

        // Crear paquetes de prueba
        Package package1 = new Package();
        package1.setQrCode("PKG001");
        package1.setLocation("Estante A3, Nivel 2");
        package1.setDescription("Electrodoméstico - Microondas");
        package1.setWarehouseSection("A");
        package1.setShelfNumber("A3-2");
        package1.setWeight(15.5);
        package1.setDimensions("45x35x30 cm");
        package1.setFragile(true);
        package1.setRoute(route1);
        packageRepository.save(package1);

        Package package2 = new Package();
        package2.setQrCode("PKG002");
        package2.setLocation("Estante B1, Nivel 1");
        package2.setDescription("Ropa - Paquete de camisetas");
        package2.setWarehouseSection("B");
        package2.setShelfNumber("B1-1");
        package2.setWeight(2.3);
        package2.setDimensions("30x25x10 cm");
        package2.setFragile(false);
        package2.setRoute(route2);
        packageRepository.save(package2);

        Package package3 = new Package();
        package3.setQrCode("PKG003");
        package3.setLocation("Estante C2, Nivel 3");
        package3.setDescription("Libros - Enciclopedia completa");
        package3.setWarehouseSection("C");
        package3.setShelfNumber("C2-3");
        package3.setWeight(8.9);
        package3.setDimensions("25x35x15 cm");
        package3.setFragile(false);
        package3.setRoute(route3);
        packageRepository.save(package3);

        System.out.println("✅ Datos de prueba inicializados:");
        System.out.println("📦 Códigos QR disponibles: PKG001, PKG002, PKG003");
        System.out.println("🚚 Rutas creadas: " + routeRepository.count());
        System.out.println("📋 Paquetes creados: " + packageRepository.count());
    }
} 