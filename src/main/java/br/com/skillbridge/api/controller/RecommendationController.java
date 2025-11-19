package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.RecommendationResponse;
import br.com.skillbridge.api.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/recomendacoes")
@Tag(name = "Recomendações")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{usuarioId}")
    @Operation(summary = "Gerar recomendações inteligentes para um usuário")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(recommendationService.generateRecommendations(usuarioId));
    }
}

