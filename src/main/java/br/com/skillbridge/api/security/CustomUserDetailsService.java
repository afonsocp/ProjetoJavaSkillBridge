package br.com.skillbridge.api.security;

import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busca o usuário do banco
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
        
        // Força refresh apenas se a entidade estiver gerenciada (evita abrir nova sessão)
        try {
            if (entityManager != null && entityManager.contains(usuario)) {
                entityManager.refresh(usuario);
            }
        } catch (Exception e) {
            // Se houver erro no refresh, retorna o usuário mesmo assim
            // Isso evita problemas durante a inicialização do Spring/Swagger
        }
        
        return usuario;
    }
}

