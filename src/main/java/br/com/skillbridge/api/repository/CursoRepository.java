package br.com.skillbridge.api.repository;

import br.com.skillbridge.api.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CursoRepository extends JpaRepository<Curso, UUID> {
}

