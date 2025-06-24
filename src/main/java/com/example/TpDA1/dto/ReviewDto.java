package com.example.TpDA1.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long routeId;
    private String comentario;
    private Integer puntuacion;
    private String imagenUrl;
}
