package br.com.skillbridge.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PlanoEstudosRequest {
    @NotBlank(message = "{validation.notblank}")
    String objetivoCarreira;
    
    @NotBlank(message = "{validation.notblank}")
    String nivelAtual; // Iniciante, Intermediário, Avançado
    
    @NotNull(message = "{validation.notnull}")
    List<String> competenciasAtuais;
    
    @Positive(message = "{validation.positive}")
    Integer tempoDisponivelSemana; // horas por semana
    
    Integer prazoMeses; // opcional, default 6
    
    List<String> areasInteresse; // opcional
}

