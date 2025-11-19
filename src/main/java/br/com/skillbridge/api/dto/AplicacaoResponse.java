package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class AplicacaoResponse {
    UUID id;
    UUID usuarioId;
    UUID vagaId;
    LocalDateTime dataAplicacao;
    String status;
    BigDecimal pontuacaoCompatibilidade;
    String comentariosAvaliador;
}

