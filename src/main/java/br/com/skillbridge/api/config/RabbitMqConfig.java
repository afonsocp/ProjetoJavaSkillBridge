package br.com.skillbridge.api.config;

import br.com.skillbridge.api.service.VagaService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitMqConfig {

    @Bean
    public Queue vagaCreatedQueue() {
        return new Queue(VagaService.VAGA_EVENT_QUEUE, true);
    }
}

