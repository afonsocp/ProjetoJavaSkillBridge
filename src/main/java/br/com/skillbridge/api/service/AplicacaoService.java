package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.AplicacaoRequest;
import br.com.skillbridge.api.dto.AplicacaoResponse;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.Aplicacao;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.AplicacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AplicacaoService {

    private final AplicacaoRepository aplicacaoRepository;
    private final UsuarioService usuarioService;
    private final VagaService vagaService;
    private final OracleProceduresGateway proceduresGateway;

    public AplicacaoService(AplicacaoRepository aplicacaoRepository,
                            UsuarioService usuarioService,
                            VagaService vagaService,
                            OracleProceduresGateway proceduresGateway) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.usuarioService = usuarioService;
        this.vagaService = vagaService;
        this.proceduresGateway = proceduresGateway;
    }

    @Transactional
    public AplicacaoResponse create(AplicacaoRequest request) {
        Usuario usuario = usuarioService.findEntityById(request.getUsuarioId());
        Vaga vaga = vagaService.findEntityById(request.getVagaId());

        proceduresGateway.registrarAplicacao(
                usuario.getId(),
                vaga.getId(),
                request.getStatus(),
                request.getPontuacaoCompatibilidade(),
                request.getComentariosAvaliador()
        );

        Aplicacao aplicacao = aplicacaoRepository.findByUsuarioIdAndVagaId(usuario.getId(), vaga.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aplicação não encontrada após registrar via procedure."));

        return mapToResponse(aplicacao);
    }

    @Transactional(readOnly = true)
    public Page<AplicacaoResponse> findAll(Pageable pageable) {
        return aplicacaoRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public AplicacaoResponse findById(UUID id) {
        return aplicacaoRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicação não encontrada."));
    }

    @Transactional
    public AplicacaoResponse update(UUID id, AplicacaoRequest request) {
        Aplicacao aplicacao = aplicacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicação não encontrada."));

        if (!aplicacao.getUsuario().getId().equals(request.getUsuarioId())) {
            Usuario usuario = usuarioService.findEntityById(request.getUsuarioId());
            aplicacao.setUsuario(usuario);
        }

        if (!aplicacao.getVaga().getId().equals(request.getVagaId())) {
            Vaga vaga = vagaService.findEntityById(request.getVagaId());
            aplicacao.setVaga(vaga);
        }

        aplicacao.setStatusAplicacao(request.getStatus());
        aplicacao.setPontuacaoCompatibilidade(request.getPontuacaoCompatibilidade());
        aplicacao.setComentariosAvaliador(request.getComentariosAvaliador());

        return mapToResponse(aplicacaoRepository.save(aplicacao));
    }

    @Transactional
    public void delete(UUID id) {
        if (!aplicacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aplicação não encontrada.");
        }
        aplicacaoRepository.deleteById(id);
    }

    private AplicacaoResponse mapToResponse(Aplicacao aplicacao) {
        return AplicacaoResponse.builder()
                .id(aplicacao.getId())
                .usuarioId(aplicacao.getUsuario().getId())
                .vagaId(aplicacao.getVaga().getId())
                .dataAplicacao(aplicacao.getDataAplicacao())
                .status(aplicacao.getStatusAplicacao())
                .pontuacaoCompatibilidade(aplicacao.getPontuacaoCompatibilidade())
                .comentariosAvaliador(aplicacao.getComentariosAvaliador())
                .build();
    }
}

