package br.com.skillbridge.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "nome", "SkillBridge API",
                "versao", "v1",
                "status", "online",
                "documentacao", "/swagger-ui.html",
                "health", "/actuator/health"
        ));
    }
}

