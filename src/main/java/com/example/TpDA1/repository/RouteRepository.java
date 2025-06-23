package com.example.TpDA1.repository;

import com.example.TpDA1.model.Route;
import com.example.TpDA1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByStatus(String status);
    List<Route> findByDriver(User driver);
    List<Route> findByDriverIsNull();
}