package br.com.skillbridge.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class DatabaseSanityCheck implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSanityCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer quantidadeUsuarios = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class);
            log.info("[Oracle] Sanity check concluído. Tabela USUARIO possui {} registro(s).", quantidadeUsuarios);
        } catch (DataAccessException ex) {
            log.warn("[Oracle] Sanity check falhou ao consultar tabela USUARIO: {}", ex.getMessage());
        }
    }
}
