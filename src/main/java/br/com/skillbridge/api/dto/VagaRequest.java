package br.com.skillbridge.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class VagaRequest {

    @NotBlank(message = "{validation.notblank}")
    private String titulo;

    @NotBlank(message = "{validation.notblank}")
    private String empresa;

    private String localidade;

    private Set<String> requisitos = new LinkedHashSet<>();

    private String responsabilidades;

    private BigDecimal salario;

    @NotBlank(message = "{validation.notblank}")
    private String tipoContrato;

    private String formatoTrabalho;

    private String nivelSenioridade;

    private LocalDateTime dataEncerramento;
}

