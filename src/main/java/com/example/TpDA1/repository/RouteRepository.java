package com.example.TpDA1.repository;

import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByStatus(String status);
    List<Route> findByDriver(User driver);
    List<Route> findByDriverIsNull();
    
    // Buscar ruta por paquete ID y email del conductor
    @Query("SELECT r FROM Route r JOIN r.packages p WHERE p.id = :packageId AND r.driver.email = :userEmail")
    Optional<Route> findByPackagesIdAndDriverEmail(@Param("packageId") Long packageId, @Param("userEmail") String userEmail);
    
    // Buscar rutas por email del conductor
    @Query("SELECT r FROM Route r WHERE r.driver.email = :userEmail")
    List<Route> findByDriverEmail(@Param("userEmail") String userEmail);
    
    // Método de debug: obtener todas las rutas
    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.driver LEFT JOIN FETCH r.packages")
    List<Route> findAllWithDriverAndPackages();
}