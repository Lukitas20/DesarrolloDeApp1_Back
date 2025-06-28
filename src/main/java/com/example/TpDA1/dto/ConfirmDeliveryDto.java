package com.example.TpDA1.dto;

import lombok.Data;

@Data
public class ConfirmDeliveryDto {
    private Long packageId;
    private String clientCode;
    private String deliveryConfirmation;
} 