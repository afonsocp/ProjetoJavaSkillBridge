package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.RecommendationResponse;
import br.com.skillbridge.api.model.Curso;
import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.StatusProfissional;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.CursoRepository;
import br.com.skillbridge.api.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private VagaRepository vagaRepository;

    private RecommendationService recommendationService;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(usuarioService, cursoRepository, vagaRepository, Optional.empty());
        usuarioId = UUID.randomUUID();
    }

    @Test
    void generateRecommendationsShouldReturnFallbackInsight() {
        Usuario usuario = Usuario.builder()
                .id(usuarioId)
                .nome("Analista Sustentável")
                .competencias(new LinkedHashSet<>(Set.of("energia renovavel", "dados")))
                .statusProfissional(StatusProfissional.ATIVO)
                .role(Role.USER)
                .build();

        Curso curso = Curso.builder()
                .id(UUID.randomUUID())
                .nome("Análise de Dados para Energia Renovável")
                .area("Energia Renovavel")
                .duracaoHoras(40)
                .modalidade("ONLINE")
                .instituicao("SkillBridge Academy")
                .descricao("Curso focado em análise de dados para projetos sustentáveis.")
                .nivel("Intermediário")
                .dataCriacao(LocalDateTime.now())
                .build();

        Vaga vaga = Vaga.builder()
                .id(UUID.randomUUID())
                .titulo("Analista de Eficiência Energética")
                .empresa("GreenCorp")
                .localidade("São Paulo/SP")
                .requisitos(new LinkedHashSet<>(Set.of("energia renovavel", "analytics")))
                .responsabilidades("Utilizar dados para reduzir consumo energético.")
                .salario(new BigDecimal("9500"))
                .tipoContrato("CLT")
                .formatoTrabalho("HIBRIDO")
                .dataPublicacao(LocalDateTime.now())
                .nivelSenioridade("Pleno")
                .build();

        when(usuarioService.findEntityById(usuarioId)).thenReturn(usuario);
        when(cursoRepository.findAll()).thenReturn(List.of(curso));
        when(vagaRepository.findAll()).thenReturn(List.of(vaga));

        RecommendationResponse response = recommendationService.generateRecommendations(usuarioId);

        assertThat(response.getCursos()).hasSize(1);
        assertThat(response.getCursos().get(0).getNome()).contains("Análise de Dados");
        assertThat(response.getVagas()).hasSize(1);
        assertThat(response.getVagas().get(0).getTitulo()).isEqualTo("Analista de Eficiência Energética");
        assertThat(response.getInsight()).isEqualTo("Ative uma chave de API compatível com Spring AI para receber insights personalizados.");
    }
}


