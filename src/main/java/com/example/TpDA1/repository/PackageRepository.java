package com.example.TpDA1.repository;

import com.example.TpDA1.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    List<Package> findByRouteId(Long routeId);
    Optional<Package> findByQrCode(String qrCode);
} 