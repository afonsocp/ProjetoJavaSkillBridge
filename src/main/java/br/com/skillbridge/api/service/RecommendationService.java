package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.CursoResponse;
import br.com.skillbridge.api.dto.RecommendationResponse;
import br.com.skillbridge.api.dto.VagaResponse;
import br.com.skillbridge.api.model.Curso;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.CursoRepository;
import br.com.skillbridge.api.repository.VagaRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UsuarioService usuarioService;
    private final CursoRepository cursoRepository;
    private final VagaRepository vagaRepository;
    private final Optional<ChatClient> chatClient;

    public RecommendationService(UsuarioService usuarioService,
                                 CursoRepository cursoRepository,
                                 VagaRepository vagaRepository,
                                 Optional<ChatClient> chatClient) {
        this.usuarioService = usuarioService;
        this.cursoRepository = cursoRepository;
        this.vagaRepository = vagaRepository;
        this.chatClient = chatClient;
    }

    public RecommendationResponse generateRecommendations(UUID usuarioId) {
        Usuario usuario = usuarioService.findEntityById(usuarioId);
        Set<String> competencias = usuario.getCompetencias().stream()
                .map(c -> c.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        List<CursoResponse> cursos = cursoRepository.findAll().stream()
                .filter(curso -> matchesCompetencias(curso.getDescricao(), competencias)
                        || matchesCompetencias(curso.getArea(), competencias)
                        || matchesCompetencias(curso.getModalidade(), competencias))
                .map(this::mapCurso)
                .limit(5)
                .toList();

        List<VagaResponse> vagas = vagaRepository.findAll().stream()
                .filter(vaga -> vaga.getRequisitos().isEmpty()
                        || vaga.getRequisitos().stream()
                        .map(req -> req.toLowerCase(Locale.ROOT))
                        .anyMatch(competencias::contains)
                        || matchesCompetencias(vaga.getResponsabilidades(), competencias))
                .map(this::mapVaga)
                .limit(5)
                .toList();

        String insight = buildInsight(usuario, cursos, vagas);

        return RecommendationResponse.builder()
                .cursos(cursos)
                .vagas(vagas)
                .insight(insight)
                .build();
    }

    private CursoResponse mapCurso(Curso curso) {
        return CursoResponse.builder()
                .id(curso.getId())
                .nome(curso.getNome())
                .area(curso.getArea())
                .duracaoHoras(curso.getDuracaoHoras())
                .modalidade(curso.getModalidade())
                .instituicao(curso.getInstituicao())
                .descricao(curso.getDescricao())
                .nivel(curso.getNivel())
                .dataCriacao(curso.getDataCriacao())
                .build();
    }

    private VagaResponse mapVaga(Vaga vaga) {
        return VagaResponse.builder()
                .id(vaga.getId())
                .titulo(vaga.getTitulo())
                .empresa(vaga.getEmpresa())
                .requisitos(vaga.getRequisitos())
                .responsabilidades(vaga.getResponsabilidades())
                .salario(vaga.getSalario())
                .tipoContrato(vaga.getTipoContrato())
                .formatoTrabalho(vaga.getFormatoTrabalho())
                .localidade(vaga.getLocalidade())
                .dataPublicacao(vaga.getDataPublicacao())
                .dataEncerramento(vaga.getDataEncerramento())
                .nivelSenioridade(vaga.getNivelSenioridade())
                .build();
    }

    private boolean matchesCompetencias(String text, Set<String> competencias) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        return competencias.stream().anyMatch(normalized::contains);
    }

    private String buildInsight(Usuario usuario, List<CursoResponse> cursos, List<VagaResponse> vagas) {
        return chatClient
                .map(client -> {
                    String prompt = """
                            Você é um consultor especializado em energia sustentável e transição energética.
                            Analise as competências do usuário e indique oportunidades de capacitação e carreiras alinhadas a energias renováveis, eficiência e descarbonização.
                            Usuário: %s
                            Competências: %s
                            Cursos sugeridos (com foco energético): %s
                            Vagas sugeridas (com foco energético): %s
                            Responda em Português do Brasil com insights objetivos, destacando próximos passos e benefícios ambientais.
                            """.formatted(
                            usuario.getNome(),
                            usuario.getCompetencias(),
                            cursos.stream()
                                    .map(c -> "%s [%s]".formatted(c.getNome(), c.getArea()))
                                    .toList(),
                            vagas.stream()
                                    .map(v -> "%s na %s [%s]".formatted(v.getTitulo(), v.getEmpresa(), v.getTipoContrato()))
                                    .toList()
                    );
                    return client.prompt()
                            .user(prompt)
                            .call()
                            .content();
                })
                .orElse("Ative uma chave de API compatível com Spring AI para receber insights personalizados.");
    }
}

