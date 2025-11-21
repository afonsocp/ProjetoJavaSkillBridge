package br.com.skillbridge.api.controller;

import br.com.skillbridge.api.dto.UsuarioRequest;
import br.com.skillbridge.api.dto.UsuarioResponse;
import br.com.skillbridge.api.exception.BusinessException;
import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/usuarios")
@Tag(name = "Usuários")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Listar usuários com paginação")
    public ResponseEntity<Page<UsuarioResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID")
    public ResponseEntity<UsuarioResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo usuário")
    public UsuarioResponse create(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.create(request, Role.USER);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário")
    public ResponseEntity<UsuarioResponse> update(@PathVariable UUID id, @RequestBody UsuarioRequest request) {
        // Permite que o usuário edite sua própria conta ou que ADMIN edite qualquer conta
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            boolean isAdmin = usuario.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwnAccount = usuario.getId().equals(id);
            
            if (!isAdmin && !isOwnAccount) {
                throw new BusinessException("Você só pode editar sua própria conta.");
            }
            
            // Apenas ADMIN pode alterar role
            if (request.getRole() != null && !isAdmin) {
                throw new BusinessException("Apenas administradores podem alterar o role de um usuário.");
            }
        }
        return ResponseEntity.ok(usuarioService.update(id, request));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar role do usuário")
    public ResponseEntity<UsuarioResponse> alterarRole(@PathVariable UUID id, @RequestBody Role role) {
        if (role == null) {
            throw new BusinessException("Role é obrigatório.");
        }
        return ResponseEntity.ok(usuarioService.alterarRole(id, role));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir usuário")
    public void delete(@PathVariable UUID id) {
        // Permite que o usuário exclua sua própria conta ou que ADMIN exclua qualquer conta
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Usuario usuario) {
            boolean isAdmin = usuario.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwnAccount = usuario.getId().equals(id);
            
            if (!isAdmin && !isOwnAccount) {
                throw new BusinessException("Você só pode excluir sua própria conta.");
            }
        }
        usuarioService.delete(id);
    }
}

