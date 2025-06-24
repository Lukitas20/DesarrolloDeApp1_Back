package com.example.TpDA1.service;

import com.example.TpDA1.model.Review;
import com.example.TpDA1.model.Route;
import com.example.TpDA1.repository.ReviewRepository;
import com.example.TpDA1.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RouteRepository routeRepository;

    public Review crearReview(Long routeId, String comentario, Integer puntuacion, MultipartFile file) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));

        // Guardar la imagen en disco
        String folder = "uploads/";
        String filename = UUID.randomUUID() + "_";
        Path path = Paths.get(folder + filename);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }

        // Crear el review
        Review review = new Review();
        review.setComentario(comentario);
        review.setPuntuacion(puntuacion);
        review.setImagenUrl(folder + filename);

        review = reviewRepository.save(review);

        // Asociar la review con la ruta
        route.setReview(review);
        routeRepository.save(route);

        return review;
    }
}
