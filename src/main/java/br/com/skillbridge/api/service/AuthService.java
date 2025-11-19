package br.com.skillbridge.api.service;

import br.com.skillbridge.api.dto.AuthResponse;
import br.com.skillbridge.api.dto.LoginRequest;
import br.com.skillbridge.api.dto.UsuarioRequest;
import br.com.skillbridge.api.dto.UsuarioResponse;
import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.repository.UsuarioRepository;
import br.com.skillbridge.api.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UsuarioService usuarioService,
                       UsuarioRepository usuarioRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AuthResponse register(UsuarioRequest request) {
        UsuarioResponse usuarioResponse = usuarioService.create(request, Role.USER);
        Usuario usuario = usuarioRepository.findById(usuarioResponse.getId()).orElseThrow();
        String token = jwtService.generateToken(usuario);
        return AuthResponse.builder()
                .token(token)
                .usuario(usuarioResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        Usuario usuario = (Usuario) authentication.getPrincipal();
        String token = jwtService.generateToken(usuario);

        UsuarioResponse response = UsuarioResponse.builder()
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

        return AuthResponse.builder()
                .token(token)
                .usuario(response)
                .build();
    }
}

