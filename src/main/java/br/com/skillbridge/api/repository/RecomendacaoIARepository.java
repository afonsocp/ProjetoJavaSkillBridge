package br.com.skillbridge.api.repository;

import br.com.skillbridge.api.model.RecomendacaoIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecomendacaoIARepository extends JpaRepository<RecomendacaoIA, UUID> {
    Optional<RecomendacaoIA> findFirstByUsuarioIdOrderByDataGeracaoDesc(UUID usuarioId);
    List<RecomendacaoIA> findByUsuarioIdOrderByDataGeracaoDesc(UUID usuarioId);
}

