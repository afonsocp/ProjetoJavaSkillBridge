package br.com.skillbridge.api.service;

import br.com.skillbridge.api.model.Role;
import br.com.skillbridge.api.model.Usuario;
import br.com.skillbridge.api.model.converter.StringSetConverter;
import br.com.skillbridge.api.model.converter.UuidToBytesConverter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
public class OracleProceduresGateway {

    private static final String PKG_USUARIOS = "PKG_USUARIOS";
    private static final String PKG_VAGAS = "PKG_VAGAS";

    private final JdbcTemplate jdbcTemplate;
    private final UuidToBytesConverter uuidConverter = new UuidToBytesConverter();
    private final StringSetConverter stringSetConverter = new StringSetConverter();

    public OracleProceduresGateway(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void inserirUsuario(Usuario usuario) {
        jdbcTemplate.execute((Connection connection) -> {
            CallableStatement cs = connection.prepareCall("{call " + PKG_USUARIOS + ".INSERIR_USUARIO(?,?,?,?,?,?,?,?,?)}");
            cs.setString(1, usuario.getNome());
            cs.setString(2, usuario.getEmail());
            cs.setString(3, usuario.getSenha());
            cs.setString(4, convertCompetencias(usuario.getCompetencias()));
            cs.setString(5, usuario.getTelefone());
            cs.setString(6, usuario.getCidade());
            cs.setString(7, usuario.getUf());
            cs.setString(8, usuario.getObjetivoCarreira());
            cs.setString(9, Objects.requireNonNullElse(usuario.getRole(), Role.USER).name());
            return cs;
        }, (CallableStatementCallback<Void>) cs -> {
            cs.execute();
            return null;
        });
    }

    public void registrarAplicacao(UUID usuarioId,
                                   UUID vagaId,
                                   String status,
                                   BigDecimal pontuacao,
                                   String comentario) {
        jdbcTemplate.execute((Connection connection) -> {
            CallableStatement cs = connection.prepareCall("{call " + PKG_VAGAS + ".REGISTRAR_APLICACAO(?,?,?,?,?)}");
            cs.setBytes(1, toRaw(usuarioId));
            cs.setBytes(2, toRaw(vagaId));
            cs.setString(3, status);
            if (pontuacao != null) {
                cs.setBigDecimal(4, pontuacao);
            } else {
                cs.setNull(4, Types.NUMERIC);
            }
            if (comentario != null && !comentario.isBlank()) {
                cs.setString(5, comentario);
            } else {
                cs.setNull(5, Types.VARCHAR);
            }
            return cs;
        }, (CallableStatementCallback<Void>) cs -> {
            cs.execute();
            return null;
        });
    }

    public BigDecimal calcularCompatibilidade(UUID vagaId, UUID usuarioId) {
        return jdbcTemplate.execute((Connection connection) -> {
            CallableStatement cs = connection.prepareCall("{ ? = call " + PKG_VAGAS + ".CALCULAR_COMPATIBILIDADE(?, ?)}");
            cs.registerOutParameter(1, Types.NUMERIC);
            cs.setBytes(2, toRaw(usuarioId));
            cs.setBytes(3, toRaw(vagaId));
            return cs;
        }, (CallableStatementCallback<BigDecimal>) cs -> {
            cs.execute();
            BigDecimal result = cs.getBigDecimal(1);
            return result != null ? result : BigDecimal.ZERO;
        });
    }

    private byte[] toRaw(UUID uuid) throws SQLException {
        if (uuid == null) {
            throw new SQLException("UUID não informado para chamada de procedure Oracle.");
        }
        return uuidConverter.convertToDatabaseColumn(uuid);
    }

    private String convertCompetencias(Set<String> competencias) {
        if (competencias == null || competencias.isEmpty()) {
            return null;
        }
        return stringSetConverter.convertToDatabaseColumn(competencias);
    }
}


