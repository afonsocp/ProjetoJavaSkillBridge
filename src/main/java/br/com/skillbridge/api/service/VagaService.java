package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.VagaRequest;
import br.com.skillbridge.api.dto.VagaResponse;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.Vaga;
import br.com.skillbridge.api.repository.VagaRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VagaService {

    public static final String VAGA_EVENT_QUEUE = "skillbridge.vaga.created";

    private final VagaRepository vagaRepository;
    private final Optional<RabbitTemplate> rabbitTemplate;
    private final UsuarioService usuarioService;
    private final OracleProceduresGateway proceduresGateway;

    public VagaService(VagaRepository vagaRepository,
                       Optional<RabbitTemplate> rabbitTemplate,
                       UsuarioService usuarioService,
                       OracleProceduresGateway proceduresGateway) {
        this.vagaRepository = vagaRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.usuarioService = usuarioService;
        this.proceduresGateway = proceduresGateway;
    }

    @Transactional
    @CacheEvict(value = "vagas", allEntries = true)
    public VagaResponse create(VagaRequest request) {
        Vaga vaga = toEntity(new Vaga(), request);
        Vaga saved = vagaRepository.save(vaga);
        
        // Tenta enviar evento para RabbitMQ, mas não falha se não estiver disponível
        rabbitTemplate.ifPresent(template -> {
            try {
                template.convertAndSend(VAGA_EVENT_QUEUE, saved.getId());
            } catch (Exception e) {
                // RabbitMQ não está disponível, mas a vaga foi criada com sucesso
                // Log do erro pode ser adicionado aqui se necessário
            }
        });
        
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "vagas", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<VagaResponse> findAll(Pageable pageable) {
        return vagaRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public VagaResponse findById(UUID id) {
        return vagaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));
    }

    @Transactional
    @CacheEvict(value = "vagas", allEntries = true)
    public VagaResponse update(UUID id, VagaRequest request) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));
        vaga = toEntity(vaga, request);
        return mapToResponse(vagaRepository.save(vaga));
    }

    @Transactional
    @CacheEvict(value = "vagas", allEntries = true)
    public void delete(UUID id) {
        if (!vagaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vaga não encontrada.");
        }
        vagaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Vaga findEntityById(UUID id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularCompatibilidade(UUID vagaId, UUID usuarioId) {
        usuarioService.findEntityById(usuarioId);
        findEntityById(vagaId);
        return proceduresGateway.calcularCompatibilidade(vagaId, usuarioId);
    }

    private VagaResponse mapToResponse(Vaga vaga) {
        return VagaResponse.builder()
                .id(vaga.getId())
                .titulo(vaga.getTitulo())
                .empresa(vaga.getEmpresa())
                .localidade(vaga.getLocalidade())
                .requisitos(vaga.getRequisitos())
                .responsabilidades(vaga.getResponsabilidades())
                .salario(vaga.getSalario())
                .tipoContrato(vaga.getTipoContrato())
                .formatoTrabalho(vaga.getFormatoTrabalho())
                .dataPublicacao(vaga.getDataPublicacao())
                .dataEncerramento(vaga.getDataEncerramento())
                .nivelSenioridade(vaga.getNivelSenioridade())
                .build();
    }

    private Vaga toEntity(Vaga vaga, VagaRequest request) {
        vaga.setTitulo(request.getTitulo());
        vaga.setEmpresa(request.getEmpresa());
        vaga.setLocalidade(request.getLocalidade());
        vaga.setRequisitos(normalizeRequisitos(request.getRequisitos()));
        vaga.setResponsabilidades(request.getResponsabilidades());
        vaga.setSalario(request.getSalario());
        vaga.setTipoContrato(request.getTipoContrato());
        vaga.setFormatoTrabalho(request.getFormatoTrabalho());
        vaga.setDataEncerramento(request.getDataEncerramento());
        vaga.setNivelSenioridade(request.getNivelSenioridade());
        return vaga;
    }

    private Set<String> normalizeRequisitos(Set<String> requisitos) {
        if (requisitos == null) {
            return new LinkedHashSet<>();
        }
        Set<String> copy = new LinkedHashSet<>(requisitos);
        copy.removeIf(String::isBlank);
        return copy;
    }
}

