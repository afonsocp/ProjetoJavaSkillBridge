package br.com.skillbridge.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import br.com.skillbridge.api.model.converter.StringSetConverter;
import br.com.skillbridge.api.model.converter.UuidToBytesConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Convert(converter = UuidToBytesConverter.class)
    @Column(name = "id", columnDefinition = "RAW(16)")
    private UUID id;

    @NotBlank(message = "{validation.notblank}")
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Email(message = "{validation.email}")
    @NotBlank(message = "{validation.notblank}")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Size(min = 6, message = "{validation.password.size}")
    @Column(name = "senha", nullable = false, length = 200)
    @JsonIgnore
    private String senha;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "cidade", length = 80)
    private String cidade;

    @Column(name = "uf", length = 2)
    private String uf;

    @Convert(converter = StringSetConverter.class)
    @Column(name = "competencias", length = 500)
    @Builder.Default
    private Set<String> competencias = new HashSet<>();

    @Column(name = "objetivo_carreira", length = 300)
    private String objetivoCarreira;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status_profissional", nullable = false, length = 30)
    private StatusProfissional statusProfissional = StatusProfissional.ATIVO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "role", nullable = false, length = 30)
    private Role role = Role.USER;

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    @JsonIgnore
    private List<Aplicacao> aplicacoes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.dataCadastro = LocalDateTime.now();
        if (statusProfissional == null) {
            statusProfissional = StatusProfissional.ATIVO;
        }
        if (role == null) {
            role = Role.USER;
        }
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return senha;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}

