package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.PlanoEstudosRequest;
import br.com.skillbridge.api.dto.PlanoEstudosResponse;
import br.com.skillbridge.api.service.PlanoEstudosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/planos-estudos")
@Tag(name = "Planos de Estudos - IA Generativa")
@SecurityRequirement(name = "bearerAuth")
public class PlanoEstudosController {

    private final PlanoEstudosService planoEstudosService;

    public PlanoEstudosController(PlanoEstudosService planoEstudosService) {
        this.planoEstudosService = planoEstudosService;
    }

    @PostMapping("/gerar")
    @Operation(summary = "Gerar plano de estudos personalizado usando IA Generativa (Gemini)")
    public ResponseEntity<PlanoEstudosResponse> gerarPlanoEstudos(
            @Valid @RequestBody PlanoEstudosRequest request) {
        return ResponseEntity.ok(planoEstudosService.gerarPlanoEstudos(request));
    }
}

