package br.com.skillbridge.api.model;

import br.com.skillbridge.api.model.converter.UuidToBytesConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "curso")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Convert(converter = UuidToBytesConverter.class)
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "area", nullable = false, length = 100)
    private String area;

    @Column(name = "duracao_horas", nullable = false)
    private Integer duracaoHoras;

    @Column(name = "modalidade", nullable = false, length = 40)
    private String modalidade;

    @Column(name = "instituicao", length = 120)
    private String instituicao;

    @Column(name = "descricao", length = 400)
    private String descricao;

    @Column(name = "nivel", length = 40)
    private String nivel;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }
}

