package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.CursoRequest;
import br.com.skillbridge.api.dto.CursoResponse;
import br.com.skillbridge.api.service.CursoService;
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
@RequestMapping("/cursos")
@Tag(name = "Cursos")
@SecurityRequirement(name = "bearerAuth")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    @Operation(summary = "Listar cursos com paginação")
    public ResponseEntity<Page<CursoResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(cursoService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar curso por ID")
    public ResponseEntity<CursoResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cursoService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo curso")
    public CursoResponse create(@Valid @RequestBody CursoRequest request) {
        return cursoService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar curso")
    public ResponseEntity<CursoResponse> update(@PathVariable UUID id, @Valid @RequestBody CursoRequest request) {
        return ResponseEntity.ok(cursoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir curso")
    public void delete(@PathVariable UUID id) {
        cursoService.delete(id);
    }
}

