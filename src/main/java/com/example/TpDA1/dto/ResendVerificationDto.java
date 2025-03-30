package com.example.TpDA1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendVerificationDto {
    private String email;
    private String codeType; // "verification" or "passwordReset"
}