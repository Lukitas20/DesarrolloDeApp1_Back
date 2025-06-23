package com.example.TpDA1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDto {
    private String email;
    private String code;
    private String newPassword;
    private String confirmPassword;
}
