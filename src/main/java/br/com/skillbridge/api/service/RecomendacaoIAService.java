package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.CursoResponse;
import br.com.skillbridge.api.dto.RecomendacaoIAResponse;
import br.com.skillbridge.api.dto.VagaResponse;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.RecomendacaoIA;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.repository.RecomendacaoIARepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RecomendacaoIAService {

    private final RecomendacaoIARepository repository;
    private final UsuarioService usuarioService;
    private final RecommendationService recommendationService;
    private final GeminiService geminiService;
    private final CursoService cursoService;
    private final VagaService vagaService;
    private final ObjectMapper objectMapper;

    public RecomendacaoIAService(RecomendacaoIARepository repository,
                                UsuarioService usuarioService,
                                RecommendationService recommendationService,
                                GeminiService geminiService,
                                CursoService cursoService,
                                VagaService vagaService,
                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.usuarioService = usuarioService;
        this.recommendationService = recommendationService;
        this.geminiService = geminiService;
        this.cursoService = cursoService;
        this.vagaService = vagaService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RecomendacaoIAResponse gerarRecomendacoes(UUID usuarioId) {
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        
        // Buscar cursos e vagas recomendados pelo serviço existente
        var recomendacoes = recommendationService.generateRecommendations(usuarioId);
        List<CursoResponse> cursos = recomendacoes.getCursos();
        List<VagaResponse> vagas = recomendacoes.getVagas();
        
        // Se não houver cursos filtrados, buscar todos os cursos disponíveis (limitado a 10)
        // para que o Gemini possa recomendar baseado no perfil do usuário
        if (cursos.isEmpty()) {
            log.info("Nenhum curso encontrado pelo filtro de competências. Buscando todos os cursos disponíveis para análise do Gemini.");
            cursos = cursoService.findAll(PageRequest.of(0, 10)).getContent();
        }
        
        // Chamar Gemini para gerar plano de carreira
        String jsonGemini = geminiService.gerarRecomendacoes(usuario, cursos, vagas);
        
        // Parsear resposta do Gemini
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonGemini);
        } catch (JsonProcessingException e) {
            log.error("Erro ao parsear JSON do Gemini", e);
            throw new RuntimeException("Erro ao processar resposta da IA", e);
        }
        
        String resumoPerfil = jsonNode.path("resumoPerfil").asText("");
        String planoCarreira = jsonNode.path("planoCarreira").asText("");
        JsonNode cursosRec = jsonNode.path("cursosRecomendados");
        JsonNode vagasRec = jsonNode.path("vagasRecomendadas");
        
        // Construir lista de cursos recomendados com detalhes
        List<RecomendacaoIAResponse.CursoRecomendadoDTO> cursosRecomendados = new ArrayList<>();
        for (JsonNode cursoNode : cursosRec) {
            try {
                UUID cursoId = UUID.fromString(cursoNode.path("id").asText());
                CursoResponse curso = cursoService.findById(cursoId);
                
                RecomendacaoIAResponse.CursoRecomendadoDTO dto = RecomendacaoIAResponse.CursoRecomendadoDTO.builder()
                    .id(curso.getId())
                    .nome(curso.getNome())
                    .area(curso.getArea())
                    .descricao(curso.getDescricao())
                    .modalidade(curso.getModalidade())
                    .instituicao(curso.getInstituicao())
                    .duracaoHoras(curso.getDuracaoHoras())
                    .nivel(curso.getNivel())
                    .motivoRecomendacao(cursoNode.path("motivoRecomendacao").asText("Recomendado pela IA"))
                    .build();
                cursosRecomendados.add(dto);
            } catch (Exception e) {
                log.warn("Erro ao processar curso recomendado: {}", e.getMessage());
            }
        }
        
        // Construir lista de vagas recomendadas com detalhes
        List<RecomendacaoIAResponse.VagaRecomendadaDTO> vagasRecomendadas = new ArrayList<>();
        for (JsonNode vagaNode : vagasRec) {
            try {
                UUID vagaId = UUID.fromString(vagaNode.path("id").asText());
                VagaResponse vaga = vagaService.findById(vagaId);
                
                RecomendacaoIAResponse.VagaRecomendadaDTO dto = RecomendacaoIAResponse.VagaRecomendadaDTO.builder()
                    .id(vaga.getId())
                    .titulo(vaga.getTitulo())
                    .empresa(vaga.getEmpresa())
                    .localidade(vaga.getLocalidade())
                    .tipoContrato(vaga.getTipoContrato())
                    .formatoTrabalho(vaga.getFormatoTrabalho())
                    .nivelSenioridade(vaga.getNivelSenioridade())
                    .motivoRecomendacao(vagaNode.path("motivoRecomendacao").asText("Recomendada pela IA"))
                    .build();
                vagasRecomendadas.add(dto);
            } catch (Exception e) {
                log.warn("Erro ao processar vaga recomendada: {}", e.getMessage());
            }
        }
        
        // Salvar no banco
        RecomendacaoIA recomendacaoIA;
        try {
            recomendacaoIA = RecomendacaoIA.builder()
                .usuario(usuario)
                .resumoPerfil(resumoPerfil)
                .planoCarreira(planoCarreira)
                .cursosRecomendados(objectMapper.writeValueAsString(cursosRecomendados))
                .vagasRecomendadas(objectMapper.writeValueAsString(vagasRecomendadas))
                .jsonCompleto(jsonGemini)
                .build();
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar recomendações para JSON", e);
            throw new RuntimeException("Erro ao salvar recomendações", e);
        }
        
        RecomendacaoIA saved = repository.save(recomendacaoIA);
        
        // Retornar resposta estruturada
        return RecomendacaoIAResponse.builder()
            .id(saved.getId())
            .usuarioId(usuarioId)
            .resumoPerfil(resumoPerfil)
            .planoCarreira(planoCarreira)
            .cursosRecomendados(cursosRecomendados)
            .vagasRecomendadas(vagasRecomendadas)
            .dataGeracao(saved.getDataGeracao())
            .build();
    }

    public RecomendacaoIAResponse buscarUltimaRecomendacao(UUID usuarioId) {
        RecomendacaoIA recomendacao = repository.findFirstByUsuarioIdOrderByDataGeracaoDesc(usuarioId)
            .orElseThrow(() -> new ResourceNotFoundException("Nenhuma recomendação encontrada para o usuário"));
        
        return mapearParaResponse(recomendacao);
    }

    private RecomendacaoIAResponse mapearParaResponse(RecomendacaoIA recomendacao) {
        try {
            List<RecomendacaoIAResponse.CursoRecomendadoDTO> cursos = objectMapper.readValue(
                recomendacao.getCursosRecomendados(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, RecomendacaoIAResponse.CursoRecomendadoDTO.class)
            );
            
            List<RecomendacaoIAResponse.VagaRecomendadaDTO> vagas = objectMapper.readValue(
                recomendacao.getVagasRecomendadas(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, RecomendacaoIAResponse.VagaRecomendadaDTO.class)
            );
            
            return RecomendacaoIAResponse.builder()
                .id(recomendacao.getId())
                .usuarioId(recomendacao.getUsuario().getId())
                .resumoPerfil(recomendacao.getResumoPerfil())
                .planoCarreira(recomendacao.getPlanoCarreira())
                .cursosRecomendados(cursos)
                .vagasRecomendadas(vagas)
                .dataGeracao(recomendacao.getDataGeracao())
                .build();
        } catch (JsonProcessingException e) {
            log.error("Erro ao mapear RecomendacaoIA para Response", e);
            throw new RuntimeException("Erro ao processar recomendação salva", e);
        }
    }
}

