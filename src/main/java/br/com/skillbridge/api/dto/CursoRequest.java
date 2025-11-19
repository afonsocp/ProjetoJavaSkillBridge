package br.com.skillbridge.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CursoRequest {
    @NotBlank(message = "{validation.notblank}")
    private String nome;

    @NotBlank(message = "{validation.notblank}")
    private String area;

    @Min(1)
    private Integer duracaoHoras;

    @NotBlank(message = "{validation.notblank}")
    private String modalidade;

    private String instituicao;

    private String descricao;

    private String nivel;
}

