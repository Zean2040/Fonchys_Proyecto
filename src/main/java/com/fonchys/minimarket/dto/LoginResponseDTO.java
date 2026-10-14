package com.fonchys.minimarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private String email;
    private String rol;
    private long expiraEnMs;
}
