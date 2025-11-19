package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.RecomendacaoIAResponse;
import br.com.skillbridge.api.service.RecomendacaoIAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ia")
@Tag(name = "IA - Recomendações")
@SecurityRequirement(name = "bearerAuth")
public class RecomendacaoIAController {

    private final RecomendacaoIAService recomendacaoIAService;

    public RecomendacaoIAController(RecomendacaoIAService recomendacaoIAService) {
        this.recomendacaoIAService = recomendacaoIAService;
    }

    @PostMapping("/recomendacoes/{usuarioId}")
    @Operation(summary = "Gerar recomendações de carreira com IA (Gemini)")
    public ResponseEntity<RecomendacaoIAResponse> gerarRecomendacoes(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(recomendacaoIAService.gerarRecomendacoes(usuarioId));
    }

    @GetMapping("/recomendacoes/{usuarioId}")
    @Operation(summary = "Buscar última recomendação gerada para o usuário")
    public ResponseEntity<RecomendacaoIAResponse> buscarUltimaRecomendacao(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(recomendacaoIAService.buscarUltimaRecomendacao(usuarioId));
    }
}

