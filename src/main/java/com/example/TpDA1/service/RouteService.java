package com.example.TpDA1.service;

import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;

    // Get all available routes (not assigned to any driver)
    public List<Route> getAvailableRoutes() {
        return routeRepository.findByStatus("AVAILABLE");
    }

    // Get routes assigned to a specific driver
    public List<Route> getDriverRoutes(User driver) {
        return routeRepository.findByDriver(driver);
    }

    // Assign a route to a driver
    public Route assignRouteToDriver(Long routeId, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (!"AVAILABLE".equals(route.getStatus())) {
            throw new RuntimeException("Route is not available for assignment");
        }

        route.setDriver(driver);
        route.setStatus("ASSIGNED");
        route.setAssignedAt(LocalDateTime.now());

        return routeRepository.save(route);
    }

    // Update route status
    public Route updateRouteStatus(Long routeId, String status, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("Route is not assigned to this driver");
        }

        route.setStatus(status);

        if ("COMPLETED".equals(status)) {
            route.setCompletedAt(LocalDateTime.now());
        }

        return routeRepository.save(route);
    }
    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route completeRoute(Long routeId, User driver) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        
        if (route.getDriver() == null || !route.getDriver().getId().equals(driver.getId())) {
            throw new RuntimeException("This route is not assigned to you");
        }
        
        if (!"ASSIGNED".equals(route.getStatus())) {
            throw new RuntimeException("Only assigned routes can be completed");
        }
        
        route.setStatus("COMPLETED");
        route.setCompletedAt(LocalDateTime.now());
        return routeRepository.save(route);
    }
}