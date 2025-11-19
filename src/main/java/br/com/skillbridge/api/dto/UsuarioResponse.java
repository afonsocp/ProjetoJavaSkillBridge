package br.com.skillbridge.api.dto;

import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.StatusProfissional;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Value
@Builder
public class UsuarioResponse {
    UUID id;
    String nome;
    String email;
    String telefone;
    String cidade;
    String uf;
    String objetivoCarreira;
    StatusProfissional statusProfissional;
    Set<String> competencias;
    LocalDateTime dataCadastro;
    Role role;
}

