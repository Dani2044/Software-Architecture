package com.sps.compra.config;

import com.sps.compra.messaging.CompraTerminadaSamEvento;
import com.sps.compra.messaging.CompraTerminadaShcEvento;
import com.sps.compra.messaging.TransaccionPago;
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
 * Configuracion RabbitMQ para MS-Compra.
 *
 * <p>Declara las tres colas del sistema SPS como beans Spring (durable),
 * configura un {@link Jackson2JsonMessageConverter} con {@link DefaultClassMapper}
 * para mapear nombres logicos a clases Java, y provee un {@link RabbitTemplate}
 * preconfigurado con el converter.</p>
 *
 * <p>Los nombres logicos ({@code CompraTerminadaSam}, {@code CompraTerminadaShc},
 * {@code TransaccionPago}) viajan en el header {@code __TypeId__} de AMQP.
 * Cada modulo consumidor (SAM, SHC) mapea el mismo nombre logico a su propia
 * clase DTO local, evitando acoplamiento por FQN.</p>
 *
 * @see com.sps.compra.messaging.IntegraSAM
 * @see com.sps.compra.messaging.IntegraSHC
 * @see com.sps.compra.messaging.ListenerPagos
 */
@Configuration
public class RabbitMQConfiguration {

    @Bean
    public Queue colaPagoConfirmado() {
        return new Queue("ColaPagoConfirmado", true);
    }

    @Bean
    public Queue colaSam() {
        return new Queue("ColaSAM", true);
    }

    @Bean
    public Queue colaShc() {
        return new Queue("ColaSHC", true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper mapper = new DefaultClassMapper();
        Map<String, Class<?>> idMappings = new HashMap<>();
        idMappings.put("CompraTerminadaSam", CompraTerminadaSamEvento.class);
        idMappings.put("CompraTerminadaShc", CompraTerminadaShcEvento.class);
        idMappings.put("TransaccionPago", TransaccionPago.class);
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
