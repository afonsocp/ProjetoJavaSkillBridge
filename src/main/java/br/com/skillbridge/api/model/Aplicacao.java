package br.com.skillbridge.api.model;

import br.com.skillbridge.api.model.converter.UuidToBytesConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "aplicacao")
public class Aplicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Convert(converter = UuidToBytesConverter.class)
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(name = "data_aplicacao", nullable = false, updatable = false)
    private LocalDateTime dataAplicacao;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "status_aplicacao", nullable = false, length = 30)
    private String statusAplicacao;

    @Column(name = "pontuacao_compatibilidade", precision = 5, scale = 2)
    private BigDecimal pontuacaoCompatibilidade;

    @Column(name = "comentarios_avaliador", length = 400)
    private String comentariosAvaliador;

    @PrePersist
    public void prePersist() {
        if (this.dataAplicacao == null) {
            this.dataAplicacao = LocalDateTime.now();
        }
        if (this.statusAplicacao == null) {
            this.statusAplicacao = "EM_ANALISE";
        }
    }
}

