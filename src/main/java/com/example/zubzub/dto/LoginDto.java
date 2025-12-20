package com.example.zubzub.dto;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class LoginDto {
    private String email;
    private String pwd;
}
