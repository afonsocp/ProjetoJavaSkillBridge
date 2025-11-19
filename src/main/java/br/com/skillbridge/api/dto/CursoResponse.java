package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class CursoResponse {
    UUID id;
    String nome;
    String area;
    Integer duracaoHoras;
    String modalidade;
    String instituicao;
    String descricao;
    String nivel;
    LocalDateTime dataCriacao;
}

