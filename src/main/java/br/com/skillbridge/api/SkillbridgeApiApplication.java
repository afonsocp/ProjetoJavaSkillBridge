package br.com.skillbridge.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableCaching
@ConfigurationPropertiesScan
@EntityScan(basePackages = "br.com.skillbridge.api.model")
@EnableJpaRepositories(basePackages = "br.com.skillbridge.api.repository")
public class SkillbridgeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillbridgeApiApplication.class, args);
    }
}
