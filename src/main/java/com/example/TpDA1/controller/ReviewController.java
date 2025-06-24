package com.example.TpDA1.controller;

import com.example.TpDA1.model.Review;
import com.example.TpDA1.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Review> crearReview(
            @RequestParam("routeId") Long routeId,
            @RequestParam("comentario") String comentario,
            @RequestParam("puntuacion") int puntuacion,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Review review = reviewService.crearReview(routeId, comentario, puntuacion, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }
}

