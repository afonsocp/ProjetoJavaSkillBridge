package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.UsuarioRequest;
import br.com.skillbridge.api.dto.UsuarioResponse;
import br.com.skillbridge.api.exception.BusinessException;
import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.StatusProfissional;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OracleProceduresGateway proceduresGateway;

    @InjectMocks
    private UsuarioService usuarioService;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private UsuarioRequest request;

    @BeforeEach
    void setUp() {
        request = new UsuarioRequest();
        request.setNome("Usuário Teste");
        request.setEmail("usuario.teste@skillbridge.com");
        request.setSenha("SenhaForte123");
        request.setTelefone("11999999999");
        request.setCidade("São Paulo");
        request.setUf("SP");
        request.setObjetivoCarreira("Validar integração");
        request.setCompetencias(new LinkedHashSet<>(Set.of("Java", "Oracle")));
    }

    @Test
    void createShouldInvokeOracleProcedureAndReturnResponse() {
        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getSenha())).thenReturn("HASH_ENCODED");

        UUID savedId = UUID.randomUUID();
        Usuario saved = Usuario.builder()
                .id(savedId)
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .cidade(request.getCidade())
                .uf(request.getUf())
                .objetivoCarreira(request.getObjetivoCarreira())
                .competencias(new LinkedHashSet<>(request.getCompetencias()))
                .dataCadastro(LocalDateTime.now())
                .role(Role.USER)
                .statusProfissional(StatusProfissional.ATIVO)
                .build();

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(saved));

        UsuarioResponse response = usuarioService.create(request, Role.USER);

        verify(usuarioRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getSenha());
        verify(proceduresGateway).inserirUsuario(usuarioCaptor.capture());

        Usuario enviado = usuarioCaptor.getValue();
        assertThat(enviado.getNome()).isEqualTo(request.getNome());
        assertThat(enviado.getSenha()).isEqualTo("HASH_ENCODED");
        assertThat(enviado.getCompetencias()).containsExactlyInAnyOrderElementsOf(request.getCompetencias());

        assertThat(response.getId()).isEqualTo(savedId);
        assertThat(response.getNome()).isEqualTo(saved.getNome());
        assertThat(response.getCompetencias()).containsExactlyInAnyOrderElementsOf(saved.getCompetencias());
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void createShouldThrowWhenPasswordMissing() {
        request.setSenha(null);
        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.create(request, Role.USER))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Senha obrigatória");
    }
}


