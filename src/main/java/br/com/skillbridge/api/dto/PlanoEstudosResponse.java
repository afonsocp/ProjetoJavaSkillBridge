package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PlanoEstudosResponse {
    String objetivoCarreira;
    String nivelAtual;
    Integer prazoTotalMeses;
    Integer horasTotaisEstimadas;
    List<EtapaEstudo> etapas;
    List<String> recursosAdicionais;
    List<String> metricasSucesso;
    String motivacao;
    
    @Value
    @Builder
    public static class EtapaEstudo {
        Integer ordem;
        String titulo;
        String descricao;
        Integer duracaoSemanas;
        List<String> recursosSugeridos;
        List<String> competenciasDesenvolvidas;
    }
}

