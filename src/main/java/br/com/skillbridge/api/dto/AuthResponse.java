package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {
    String token;
    UsuarioResponse usuario;
}

