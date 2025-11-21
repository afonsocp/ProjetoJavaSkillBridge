package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.AplicacaoRequest;
import br.com.skillbridge.api.dto.AplicacaoResponse;
import br.com.skillbridge.api.service.AplicacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/aplicacoes")
@Tag(name = "Aplicações")
@SecurityRequirement(name = "bearerAuth")
public class AplicacaoController {

    private final AplicacaoService aplicacaoService;

    public AplicacaoController(AplicacaoService aplicacaoService) {
        this.aplicacaoService = aplicacaoService;
    }

    @GetMapping
    @Operation(summary = "Listar aplicações com paginação")
    public ResponseEntity<Page<AplicacaoResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(aplicacaoService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar aplicação por ID")
    public ResponseEntity<AplicacaoResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(aplicacaoService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar nova aplicação")
    public AplicacaoResponse create(@Valid @RequestBody AplicacaoRequest request) {
        return aplicacaoService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar aplicação")
    public ResponseEntity<AplicacaoResponse> update(@PathVariable UUID id, @Valid @RequestBody AplicacaoRequest request) {
        return ResponseEntity.ok(aplicacaoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir aplicação")
    public void delete(@PathVariable UUID id) {
        aplicacaoService.delete(id);
    }
}

