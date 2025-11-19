package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.UsuarioRequest;
import br.com.skillbridge.api.dto.UsuarioResponse;
import br.com.skillbridge.api.exception.BusinessException;
import br.com.skillbridge.api.exception.ResourceNotFoundException;
import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.StatusProfissional;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OracleProceduresGateway proceduresGateway;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          OracleProceduresGateway proceduresGateway) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.proceduresGateway = proceduresGateway;
    }

    @Transactional
    public UsuarioResponse create(UsuarioRequest request, Role role) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }
        if (!StringUtils.hasText(request.getSenha())) {
            throw new BusinessException("Senha obrigatória.");
        }
        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .competencias(normalizeCompetencias(request.getCompetencias()))
                .telefone(request.getTelefone())
                .cidade(request.getCidade())
                .uf(formatUf(request.getUf()))
                .objetivoCarreira(request.getObjetivoCarreira())
                .statusProfissional(request.getStatusProfissional() == null ? StatusProfissional.ATIVO : request.getStatusProfissional())
                .role(role == null ? Role.USER : role)
                .build();

        proceduresGateway.inserirUsuario(usuario);

        Usuario saved = usuarioRepository.findByEmail(usuario.getEmail())
                .orElseThrow(() -> new BusinessException("Falha ao inserir usuário via procedure."));

        if (usuario.getStatusProfissional() != null && usuario.getStatusProfissional() != StatusProfissional.ATIVO) {
            saved.setStatusProfissional(usuario.getStatusProfissional());
            saved = usuarioRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> findAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findById(UUID id) {
        return usuarioRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Transactional
    public UsuarioResponse update(UUID id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());

        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        usuario.setCompetencias(normalizeCompetencias(request.getCompetencias()));
        usuario.setTelefone(request.getTelefone());
        usuario.setCidade(request.getCidade());
        usuario.setUf(formatUf(request.getUf()));
        if (request.getObjetivoCarreira() != null) {
            usuario.setObjetivoCarreira(request.getObjetivoCarreira());
        }
        if (request.getStatusProfissional() != null) {
            usuario.setStatusProfissional(request.getStatusProfissional());
        }

        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void delete(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Usuario findEntityById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .cidade(usuario.getCidade())
                .uf(usuario.getUf())
                .objetivoCarreira(usuario.getObjetivoCarreira())
                .statusProfissional(usuario.getStatusProfissional())
                .competencias(usuario.getCompetencias())
                .dataCadastro(usuario.getDataCadastro())
                .role(usuario.getRole())
                .build();
    }

    private Set<String> normalizeCompetencias(Set<String> competencias) {
        if (competencias == null) {
            return new LinkedHashSet<>();
        }
        Set<String> copy = new LinkedHashSet<>(competencias);
        copy.removeIf(String::isBlank);
        return copy;
    }

    private String formatUf(String uf) {
        if (!StringUtils.hasText(uf)) {
            return null;
        }
        String trimmed = uf.trim();
        if (trimmed.length() > 2) {
            throw new BusinessException("UF deve conter no máximo 2 caracteres.");
        }
        return trimmed.toUpperCase();
    }
}

