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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "recomendacao_ia")
public class RecomendacaoIA {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Convert(converter = UuidToBytesConverter.class)
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Lob
    @Column(name = "resumo_perfil", columnDefinition = "CLOB")
    private String resumoPerfil;

    @Lob
    @Column(name = "plano_carreira", columnDefinition = "CLOB")
    private String planoCarreira;

    @Lob
    @Column(name = "cursos_recomendados", columnDefinition = "CLOB")
    private String cursosRecomendados; // JSON array

    @Lob
    @Column(name = "vagas_recomendadas", columnDefinition = "CLOB")
    private String vagasRecomendadas; // JSON array

    @Lob
    @Column(name = "json_completo", columnDefinition = "CLOB")
    private String jsonCompleto; // JSON completo da resposta do Gemini

    @Column(name = "data_geracao", nullable = false, updatable = false)
    private LocalDateTime dataGeracao;

    @PrePersist
    public void prePersist() {
        if (this.dataGeracao == null) {
            this.dataGeracao = LocalDateTime.now();
        }
    }
}

