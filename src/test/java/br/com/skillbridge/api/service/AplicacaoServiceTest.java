package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.AplicacaoRequest;
import br.com.skillbridge.api.dto.AplicacaoResponse;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.Aplicacao;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.AplicacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AplicacaoServiceTest {

    @Mock
    private AplicacaoRepository aplicacaoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private VagaService vagaService;

    @Mock
    private OracleProceduresGateway proceduresGateway;

    @InjectMocks
    private AplicacaoService aplicacaoService;

    private UUID usuarioId;
    private UUID vagaId;
    private Usuario usuario;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        vagaId = UUID.randomUUID();

        usuario = Usuario.builder().id(usuarioId).build();
        vaga = Vaga.builder().id(vagaId).build();
    }

    @Test
    void createShouldInvokeProcedureAndReturnResponse() {
        AplicacaoRequest request = new AplicacaoRequest();
        request.setUsuarioId(usuarioId);
        request.setVagaId(vagaId);
        request.setStatus("EM_ANALISE");
        request.setPontuacaoCompatibilidade(new BigDecimal("87.5"));
        request.setComentariosAvaliador("Integração aprovada");

        Aplicacao aplicacao = Aplicacao.builder()
                .id(UUID.randomUUID())
                .usuario(usuario)
                .vaga(vaga)
                .dataAplicacao(LocalDateTime.now())
                .statusAplicacao(request.getStatus())
                .pontuacaoCompatibilidade(request.getPontuacaoCompatibilidade())
                .comentariosAvaliador(request.getComentariosAvaliador())
                .build();

        when(usuarioService.findEntityById(usuarioId)).thenReturn(usuario);
        when(vagaService.findEntityById(vagaId)).thenReturn(vaga);
        when(aplicacaoRepository.findByUsuarioIdAndVagaId(usuarioId, vagaId)).thenReturn(Optional.of(aplicacao));

        AplicacaoResponse response = aplicacaoService.create(request);

        verify(proceduresGateway).registrarAplicacao(
                usuarioId,
                vagaId,
                request.getStatus(),
                request.getPontuacaoCompatibilidade(),
                request.getComentariosAvaliador()
        );

        assertThat(response.getId()).isEqualTo(aplicacao.getId());
        assertThat(response.getStatus()).isEqualTo(request.getStatus());
        assertThat(response.getPontuacaoCompatibilidade()).isEqualTo(request.getPontuacaoCompatibilidade());
        assertThat(response.getComentariosAvaliador()).isEqualTo(request.getComentariosAvaliador());
    }

    @Test
    void createShouldThrowWhenProcedureDoesNotPersist() {
        AplicacaoRequest request = new AplicacaoRequest();
        request.setUsuarioId(usuarioId);
        request.setVagaId(vagaId);
        request.setStatus("EM_ANALISE");

        when(usuarioService.findEntityById(usuarioId)).thenReturn(usuario);
        when(vagaService.findEntityById(vagaId)).thenReturn(vaga);
        when(aplicacaoRepository.findByUsuarioIdAndVagaId(usuarioId, vagaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aplicacaoService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Aplicação não encontrada");
    }
}



