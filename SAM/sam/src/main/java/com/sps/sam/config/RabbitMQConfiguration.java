package com.sps.sam.config;

import com.sps.sam.dto.CompraTerminadaSamDto;
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
 * Configuracion RabbitMQ para SAM.
 *
 * <p>Declara la cola {@code ColaSAM} como bean durable y configura el
 * {@link Jackson2JsonMessageConverter} con {@link DefaultClassMapper} para
 * mapear el nombre logico {@code CompraTerminadaSam} (enviado por MS-Compra
 * en el header {@code __TypeId__}) a la clase local {@link CompraTerminadaSamDto}.</p>
 *
 * @see com.sps.sam.listener.ListenerSAM
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue colaSam() {
        return new Queue("ColaSAM", true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper mapper = new DefaultClassMapper();
        Map<String, Class<?>> idMappings = new HashMap<>();
        idMappings.put("CompraTerminadaSam", CompraTerminadaSamDto.class);
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
