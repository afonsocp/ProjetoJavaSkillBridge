package br.com.skillbridge.api.repository;

import br.com.skillbridge.api.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VagaRepository extends JpaRepository<Vaga, UUID> {
}

