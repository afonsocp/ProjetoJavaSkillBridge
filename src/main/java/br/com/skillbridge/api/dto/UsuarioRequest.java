package br.com.skillbridge.api.dto;

import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.StatusProfissional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UsuarioRequest {

    private String nome;

    @Email(message = "{validation.email}")
    private String email;

    @Size(min = 6, message = "{validation.password.size}")
    private String senha;

    private String telefone;

    private String cidade;

    @Size(max = 2, message = "{validation.uf.size}")
    private String uf;

    private String objetivoCarreira;

    private StatusProfissional statusProfissional;

    private Set<String> competencias;
    
    private Role role;
}

