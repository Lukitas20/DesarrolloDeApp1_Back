package com.example.TpDA1.repository;

import com.example.TpDA1.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByRouteId(Long routeId);
}

