package br.com.skillbridge.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "{validation.email}")
    @NotBlank(message = "{validation.notblank}")
    private String email;

    @NotBlank(message = "{validation.notblank}")
    private String senha;
}

