package com.sps.shc.config;

import com.sps.shc.dto.CompraTerminadaShcDto;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuracion RabbitMQ para SHC.
 *
 * <p>Declara la cola {@code ColaSHC} como bean durable y configura el
 * {@link Jackson2JsonMessageConverter} con {@link DefaultClassMapper} para
 * mapear el nombre logico {@code CompraTerminadaShc} (enviado por MS-Compra
 * en el header {@code __TypeId__}) a la clase local {@link CompraTerminadaShcDto}.</p>
 *
 * @see com.sps.shc.listener.ListenerSHC
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue colaShc() {
        return new Queue("ColaSHC", true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper mapper = new DefaultClassMapper();
        Map<String, Class<?>> idMappings = new HashMap<>();
        idMappings.put("CompraTerminadaShc", CompraTerminadaShcDto.class);
        mapper.setIdClassMapping(idMappings);
        converter.setClassMapper(mapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
