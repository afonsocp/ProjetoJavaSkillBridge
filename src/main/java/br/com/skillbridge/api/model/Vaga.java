package br.com.skillbridge.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import br.com.skillbridge.api.model.converter.StringSetConverter;
import br.com.skillbridge.api.model.converter.UuidToBytesConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vaga")
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Convert(converter = UuidToBytesConverter.class)
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "empresa", nullable = false, length = 120)
    private String empresa;

    @Column(name = "localidade", length = 100)
    private String localidade;

    @Convert(converter = StringSetConverter.class)
    @Column(name = "requisitos", length = 500)
    @Builder.Default
    private Set<String> requisitos = new HashSet<>();

    @Column(name = "responsabilidades", length = 500)
    private String responsabilidades;

    @Column(name = "salario", precision = 10, scale = 2)
    private BigDecimal salario;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "tipo_contrato", nullable = false, length = 30)
    private String tipoContrato;

    @Column(name = "formato_trabalho", length = 30)
    private String formatoTrabalho;

    @Column(name = "data_publicacao", updatable = false)
    private LocalDateTime dataPublicacao;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    @Column(name = "nivel_senioridade", length = 30)
    private String nivelSenioridade;

    @OneToMany(mappedBy = "vaga")
    @Builder.Default
    @JsonIgnore
    private List<Aplicacao> aplicacoes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dataPublicacao == null) {
            dataPublicacao = LocalDateTime.now();
        }
    }
}

