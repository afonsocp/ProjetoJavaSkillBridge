package br.com.skillbridge.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class RecomendacaoIAResponse {
    UUID id;
    UUID usuarioId;
    
    @JsonProperty("resumoPerfil")
    String resumoPerfil;
    
    @JsonProperty("planoCarreira")
    String planoCarreira;
    
    @JsonProperty("cursosRecomendados")
    List<CursoRecomendadoDTO> cursosRecomendados;
    
    @JsonProperty("vagasRecomendadas")
    List<VagaRecomendadaDTO> vagasRecomendadas;
    
    LocalDateTime dataGeracao;
    
    @Value
    @Builder
    public static class CursoRecomendadoDTO {
        UUID id;
        String nome;
        String area;
        String descricao;
        String modalidade;
        String instituicao;
        Integer duracaoHoras;
        String nivel;
        String motivoRecomendacao;
    }
    
    @Value
    @Builder
    public static class VagaRecomendadaDTO {
        UUID id;
        String titulo;
        String empresa;
        String localidade;
        String tipoContrato;
        String formatoTrabalho;
        String nivelSenioridade;
        String motivoRecomendacao;
    }
}

