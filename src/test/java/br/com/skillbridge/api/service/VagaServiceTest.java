package br.com.skillbridge.api.service;

import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VagaServiceTest {

    @Mock
    private VagaRepository vagaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private OracleProceduresGateway proceduresGateway;

    private VagaService vagaService;

    @BeforeEach
    void setup() {
        vagaService = new VagaService(vagaRepository, Optional.empty(), usuarioService, proceduresGateway);
    }

    @Test
    void calcularCompatibilidadeShouldDelegateToOraclePackage() {
        UUID vagaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        when(usuarioService.findEntityById(usuarioId)).thenReturn(Usuario.builder().id(usuarioId).build());
        when(vagaRepository.findById(vagaId)).thenReturn(Optional.of(Vaga.builder().id(vagaId).build()));
        when(proceduresGateway.calcularCompatibilidade(vagaId, usuarioId))
                .thenReturn(new BigDecimal("72.55"));

        BigDecimal resultado = vagaService.calcularCompatibilidade(vagaId, usuarioId);

        verify(proceduresGateway).calcularCompatibilidade(vagaId, usuarioId);
        assertThat(resultado).isEqualTo(new BigDecimal("72.55"));
    }
}


