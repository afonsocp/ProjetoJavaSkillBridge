package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.CursoResponse;
import br.com.skillbridge.api.dto.VagaResponse;
import br.com.skillbridge.api.exception.BusinessException;
import br.com.skillbridge.api.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${spring.ai.gemini.api-key:}")
    private String geminiApiKey;
    
    @Value("${spring.ai.gemini.model:gemini-2.0-flash-exp}")
    private String geminiModel;
    
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String gerarRecomendacoes(Usuario usuario, List<CursoResponse> cursos, List<VagaResponse> vagas) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("GEMINI_API_KEY não configurada. Retornando resposta simulada.");
            return gerarRespostaSimulada(usuario, cursos, vagas);
        }

        try {
            String prompt = construirPrompt(usuario, cursos, vagas);
            String responseJson = chamarGeminiAPI(prompt);
            return parsearRespostaGemini(responseJson);
        } catch (Exception e) {
            log.error("Erro ao chamar API do Gemini: {}", e.getMessage(), e);
            return gerarRespostaSimulada(usuario, cursos, vagas);
        }
    }

    private String construirPrompt(Usuario usuario, List<CursoResponse> cursos, List<VagaResponse> vagas) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Você é um consultor de carreira especializado em requalificação profissional e empregabilidade.\n\n");
        prompt.append("Analise o perfil do usuário abaixo e gere um plano de carreira personalizado com recomendações de cursos e vagas.\n\n");
        
        prompt.append("PERFIL DO USUÁRIO:\n");
        prompt.append("- Nome: ").append(usuario.getNome()).append("\n");
        prompt.append("- Email: ").append(usuario.getEmail()).append("\n");
        prompt.append("- Cidade: ").append(usuario.getCidade() != null ? usuario.getCidade() : "Não informado").append("\n");
        prompt.append("- UF: ").append(usuario.getUf() != null ? usuario.getUf() : "Não informado").append("\n");
        prompt.append("- Objetivo de Carreira: ").append(usuario.getObjetivoCarreira() != null ? usuario.getObjetivoCarreira() : "Não informado").append("\n");
        prompt.append("- Competências: ").append(String.join(", ", usuario.getCompetencias())).append("\n");
        prompt.append("- Status Profissional: ").append(usuario.getStatusProfissional()).append("\n\n");
        
        prompt.append("CURSOS DISPONÍVEIS:\n");
        for (CursoResponse curso : cursos) {
            prompt.append(String.format("- ID: %s | %s (%s) - %s horas - %s - %s\n", 
                curso.getId().toString(),
                curso.getNome(), curso.getArea(), curso.getDuracaoHoras(), 
                curso.getModalidade(), curso.getDescricao() != null ? curso.getDescricao() : ""));
        }
        prompt.append("\n");
        
        prompt.append("VAGAS DISPONÍVEIS:\n");
        for (VagaResponse vaga : vagas) {
            prompt.append(String.format("- %s na %s (%s) - %s - Requisitos: %s\n",
                vaga.getTitulo(), vaga.getEmpresa(), vaga.getLocalidade() != null ? vaga.getLocalidade() : "",
                vaga.getTipoContrato(), String.join(", ", vaga.getRequisitos())));
        }
        prompt.append("\n");
        
        prompt.append("TAREFAS:\n");
        prompt.append("1. Gere um RESUMO DO PERFIL do usuário (máximo 150 palavras)\n");
        prompt.append("2. Crie um PLANO DE CARREIRA personalizado com etapas práticas (máximo 300 palavras)\n");
        prompt.append("3. Recomende até 5 cursos mais adequados ao perfil, explicando o motivo de cada recomendação\n");
        prompt.append("4. Recomende até 5 vagas mais adequadas ao perfil, explicando o motivo de cada recomendação\n\n");
        
        prompt.append("RESPONDA APENAS COM UM JSON VÁLIDO NO SEGUINTE FORMATO:\n");
        prompt.append("{\n");
        prompt.append("  \"resumoPerfil\": \"...\",\n");
        prompt.append("  \"planoCarreira\": \"...\",\n");
        prompt.append("  \"cursosRecomendados\": [\n");
        prompt.append("    {\"id\": \"uuid-do-curso\", \"motivoRecomendacao\": \"...\"},\n");
        prompt.append("    ...\n");
        prompt.append("  ],\n");
        prompt.append("  \"vagasRecomendadas\": [\n");
        prompt.append("    {\"id\": \"uuid-da-vaga\", \"motivoRecomendacao\": \"...\"},\n");
        prompt.append("    ...\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\nIMPORTANTE:\n");
        prompt.append("- Use APENAS os IDs dos cursos e vagas listados acima\n");
        prompt.append("- Recomende até 5 cursos mais relevantes para o perfil do usuário\n");
        prompt.append("- Recomende até 5 vagas mais adequadas ao perfil\n");
        prompt.append("- Responda APENAS o JSON, sem markdown, sem explicações adicionais\n");
        prompt.append("- Se não houver cursos/vagas adequados, retorne arrays vazios []");

        return prompt.toString();
    }

    private String chamarGeminiAPI(String prompt) {
        String url = String.format(GEMINI_API_URL, geminiModel, geminiApiKey);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);
        content.put("parts", List.of(part));
        requestBody.put("contents", List.of(content));
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, 
            new ParameterizedTypeReference<Map<String, Object>>() {});
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return objectMapper.writeValueAsString(response.getBody());
            } catch (JsonProcessingException e) {
                throw new BusinessException("Erro ao processar resposta do Gemini: " + e.getMessage());
            }
        } else {
            throw new BusinessException("Erro ao chamar API do Gemini. Status: " + response.getStatusCode());
        }
    }

    private String parsearRespostaGemini(String responseJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidates = root.path("candidates");
        
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content");
            JsonNode parts = content.path("parts");
            
            if (parts.isArray() && parts.size() > 0) {
                String text = parts.get(0).path("text").asText();
                // Limpar markdown code blocks se existirem
                return text.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
            }
        }
        
        throw new BusinessException("Resposta do Gemini não contém conteúdo válido");
    }

    private String gerarRespostaSimulada(Usuario usuario, List<CursoResponse> cursos, List<VagaResponse> vagas) {
        try {
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("resumoPerfil", 
                String.format("Perfil de %s com competências em %s. Objetivo: %s", 
                    usuario.getNome(), 
                    String.join(", ", usuario.getCompetencias()),
                    usuario.getObjetivoCarreira() != null ? usuario.getObjetivoCarreira() : "Desenvolvimento profissional"));
            
            resposta.put("planoCarreira", 
                "1. Fortalecer competências técnicas através de cursos especializados\n" +
                "2. Buscar oportunidades alinhadas ao perfil profissional\n" +
                "3. Desenvolver habilidades complementares para crescimento na carreira");
            
            List<Map<String, Object>> cursosRec = cursos.stream()
                .limit(5)
                .map(c -> {
                    Map<String, Object> curso = new HashMap<>();
                    curso.put("id", c.getId().toString());
                    curso.put("motivoRecomendacao", "Alinhado com suas competências em " + String.join(", ", usuario.getCompetencias()));
                    return curso;
                })
                .toList();
            resposta.put("cursosRecomendados", cursosRec);
            
            List<Map<String, Object>> vagasRec = vagas.stream()
                .limit(5)
                .map(v -> {
                    Map<String, Object> vaga = new HashMap<>();
                    vaga.put("id", v.getId().toString());
                    vaga.put("motivoRecomendacao", "Compatível com seu perfil e objetivos profissionais");
                    return vaga;
                })
                .toList();
            resposta.put("vagasRecomendadas", vagasRec);
            
            return objectMapper.writeValueAsString(resposta);
        } catch (JsonProcessingException e) {
            log.error("Erro ao gerar resposta simulada", e);
            return "{\"erro\": \"Não foi possível gerar recomendações\"}";
        }
    }
}

