package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.VagaRequest;
import br.com.skillbridge.api.dto.VagaResponse;
import br.com.skillbridge.api.service.VagaService;
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
@RequestMapping("/vagas")
@Tag(name = "Vagas")
@SecurityRequirement(name = "bearerAuth")
public class VagaController {

    private final VagaService vagaService;

    public VagaController(VagaService vagaService) {
        this.vagaService = vagaService;
    }

    @GetMapping
    @Operation(summary = "Listar vagas com paginação")
    public ResponseEntity<Page<VagaResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(vagaService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vaga por ID")
    public ResponseEntity<VagaResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(vagaService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar nova vaga")
    public VagaResponse create(@Valid @RequestBody VagaRequest request) {
        return vagaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar vaga")
    public ResponseEntity<VagaResponse> update(@PathVariable UUID id, @Valid @RequestBody VagaRequest request) {
        return ResponseEntity.ok(vagaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir vaga")
    public void delete(@PathVariable UUID id) {
        vagaService.delete(id);
    }
}

