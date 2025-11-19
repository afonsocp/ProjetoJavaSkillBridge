package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.PlanoEstudosRequest;
import br.com.skillbridge.api.dto.PlanoEstudosResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlanoEstudosService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${iot.service.url:http://localhost:8000}")
    private String iotServiceUrl;

    public PlanoEstudosService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public PlanoEstudosResponse gerarPlanoEstudos(PlanoEstudosRequest request) {
        try {
            String url = iotServiceUrl + "/gerar-plano-estudos";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Converter camelCase para snake_case para o Python
            Map<String, Object> pythonRequest = new HashMap<>();
            pythonRequest.put("objetivo_carreira", request.getObjetivoCarreira());
            pythonRequest.put("nivel_atual", request.getNivelAtual());
            pythonRequest.put("competencias_atuais", request.getCompetenciasAtuais());
            pythonRequest.put("tempo_disponivel_semana", request.getTempoDisponivelSemana());
            if (request.getPrazoMeses() != null) {
                pythonRequest.put("prazo_meses", request.getPrazoMeses());
            }
            if (request.getAreasInteresse() != null && !request.getAreasInteresse().isEmpty()) {
                pythonRequest.put("areas_interesse", request.getAreasInteresse());
            }
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(pythonRequest, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return mapearResposta(jsonNode, request);
            } else {
                throw new RuntimeException("Erro ao chamar serviço IOT: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Erro ao gerar plano de estudos", e);
            throw new RuntimeException("Erro ao gerar plano de estudos: " + e.getMessage(), e);
        }
    }

    private PlanoEstudosResponse mapearResposta(JsonNode jsonNode, PlanoEstudosRequest request) {
        List<PlanoEstudosResponse.EtapaEstudo> etapas = new ArrayList<>();
        
        if (jsonNode.has("etapas")) {
            jsonNode.get("etapas").forEach(etapaNode -> {
                List<String> recursos = new ArrayList<>();
                if (etapaNode.has("recursos_sugeridos")) {
                    etapaNode.get("recursos_sugeridos").forEach(r -> recursos.add(r.asText()));
                }
                
                List<String> competencias = new ArrayList<>();
                if (etapaNode.has("competencias_desenvolvidas")) {
                    etapaNode.get("competencias_desenvolvidas").forEach(c -> competencias.add(c.asText()));
                }
                
                PlanoEstudosResponse.EtapaEstudo etapa = PlanoEstudosResponse.EtapaEstudo.builder()
                    .ordem(etapaNode.path("ordem").asInt(0))
                    .titulo(etapaNode.path("titulo").asText(""))
                    .descricao(etapaNode.path("descricao").asText(""))
                    .duracaoSemanas(etapaNode.path("duracao_semanas").asInt(2))
                    .recursosSugeridos(recursos)
                    .competenciasDesenvolvidas(competencias)
                    .build();
                
                etapas.add(etapa);
            });
        }
        
        List<String> recursosAdicionais = new ArrayList<>();
        if (jsonNode.has("recursos_adicionais")) {
            jsonNode.get("recursos_adicionais").forEach(r -> recursosAdicionais.add(r.asText()));
        }
        
        List<String> metricasSucesso = new ArrayList<>();
        if (jsonNode.has("metricas_sucesso")) {
            jsonNode.get("metricas_sucesso").forEach(m -> metricasSucesso.add(m.asText()));
        }
        
        return PlanoEstudosResponse.builder()
            .objetivoCarreira(jsonNode.path("objetivo_carreira").asText(request.getObjetivoCarreira()))
            .nivelAtual(jsonNode.path("nivel_atual").asText(request.getNivelAtual()))
            .prazoTotalMeses(jsonNode.path("prazo_total_meses").asInt(request.getPrazoMeses()))
            .horasTotaisEstimadas(jsonNode.path("horas_totais_estimadas").asInt(0))
            .etapas(etapas)
            .recursosAdicionais(recursosAdicionais)
            .metricasSucesso(metricasSucesso)
            .motivacao(jsonNode.path("motivacao").asText("Continue estudando!"))
            .build();
    }
}

