package br.com.skillbridge.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class AplicacaoRequest {
    @NotNull
    private UUID usuarioId;

    @NotNull
    private UUID vagaId;

    @NotBlank(message = "{validation.notblank}")
    private String status;

    private BigDecimal pontuacaoCompatibilidade;

    private String comentariosAvaliador;
}

