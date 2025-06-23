package com.example.TpDA1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyResetCodeDto {
    private String email;
    private String code;
}
