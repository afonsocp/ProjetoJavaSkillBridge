package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class VagaResponse {
    UUID id;
    String titulo;
    String empresa;
    String localidade;
    Set<String> requisitos;
    String responsabilidades;
    BigDecimal salario;
    String tipoContrato;
    String formatoTrabalho;
    LocalDateTime dataPublicacao;
    LocalDateTime dataEncerramento;
    String nivelSenioridade;
}

