package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.CursoRequest;
import br.com.skillbridge.api.dto.CursoResponse;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.Curso;
import br.com.skillbridge.api.repository.CursoRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    @CacheEvict(value = "cursos", allEntries = true)
    public CursoResponse create(CursoRequest request) {
        Curso curso = toEntity(new Curso(), request);
        return mapToResponse(cursoRepository.save(curso));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "cursos", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<CursoResponse> findAll(Pageable pageable) {
        return cursoRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public CursoResponse findById(UUID id) {
        return cursoRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado."));
    }

    @Transactional
    @CacheEvict(value = "cursos", allEntries = true)
    public CursoResponse update(UUID id, CursoRequest request) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado."));
        curso = toEntity(curso, request);
        return mapToResponse(cursoRepository.save(curso));
    }

    @Transactional
    @CacheEvict(value = "cursos", allEntries = true)
    public void delete(UUID id) {
        if (!cursoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Curso não encontrado.");
        }
        cursoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Curso findEntityById(UUID id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado."));
    }

    private CursoResponse mapToResponse(Curso curso) {
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

    private Curso toEntity(Curso curso, CursoRequest request) {
        curso.setNome(request.getNome());
        curso.setArea(request.getArea());
        curso.setDuracaoHoras(request.getDuracaoHoras());
        curso.setModalidade(request.getModalidade());
        curso.setInstituicao(request.getInstituicao());
        curso.setDescricao(request.getDescricao());
        curso.setNivel(request.getNivel());
        return curso;
    }
}

