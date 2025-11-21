package br.com.skillbridge.api.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class HomeController {

    @GetMapping("/")
    @Operation(summary = "Home - Informações da API")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "nome", "SkillBridge API",
                "versao", "v1",
                "status", "online",
                "documentacao", "/swagger-ui.html",
                "health", "/actuator/health"
        ));
    }

    @GetMapping("/debug/auth")
    @Hidden
    public ResponseEntity<Map<String, Object>> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "message", "Nenhum usuário autenticado"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                "principal", auth.getPrincipal().getClass().getSimpleName()
        ));
    }
}

