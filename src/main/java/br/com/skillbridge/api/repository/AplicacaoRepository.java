package br.com.skillbridge.api.repository;

import br.com.skillbridge.api.model.Aplicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AplicacaoRepository extends JpaRepository<Aplicacao, UUID> {

    Optional<Aplicacao> findByUsuarioIdAndVagaId(UUID usuarioId, UUID vagaId);
}

