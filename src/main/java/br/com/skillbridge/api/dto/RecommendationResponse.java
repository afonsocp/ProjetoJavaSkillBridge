package br.com.skillbridge.api.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RecommendationResponse {
    @Singular
    List<CursoResponse> cursos;
    @Singular
    List<VagaResponse> vagas;
    String insight;
}

