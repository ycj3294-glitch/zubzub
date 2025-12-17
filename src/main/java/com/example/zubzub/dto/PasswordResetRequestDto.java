package com.example.zubzub.dto;

import lombok.Getter;
import lombok.Setter;

// DTO 클래스
@Getter
@Setter
public class PasswordResetRequestDto {
    private String email;
}