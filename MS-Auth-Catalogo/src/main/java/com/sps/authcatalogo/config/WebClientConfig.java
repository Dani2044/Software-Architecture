package com.sps.authcatalogo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuracion del {@link RestClient} utilizado por
 * {@link com.sps.authcatalogo.catalogo.ProxyCatalogo} para reenviar
 * las peticiones de compra al Balanceador del sistema SPS.
 *
 * <p>El valor por defecto apunta al Balanceador en la VM de Daniel
 * ({@code http://10.43.101.18:8080}). Puede sobrescribirse mediante
 * la variable de entorno {@code BALANCEADOR_URL} o la propiedad
 * {@code balanceador.url} en {@code application.yml}.</p>
 *
 * <p>Se usa {@link RestClient} (Spring 6+) en lugar de {@code WebClient}
 * porque el modulo MS-Auth-Catalogo es Spring MVC (sincronico) y
 * {@code RestClient} viene incluido con {@code spring-web} sin
 * requerir la dependencia adicional de WebFlux.</p>
 *
 * @author SPS Team
 */
@Configuration
public class WebClientConfig {

    /** URL base del Balanceador, configurable via {@code balanceador.url}. */
    @Value("${balanceador.url:http://10.43.101.18:8080}")
    private String balanceadorUrl;

    /**
     * Bean {@link RestClient} preconfigurado con la URL base del Balanceador.
     *
     * <p>Usado por {@link com.sps.authcatalogo.catalogo.ProxyCatalogo} para
     * reenviar las peticiones de compra ({@code /api/compra/**}) al
     * {@code WSIPVirtual} del modulo Balanceador.</p>
     *
     * @return cliente HTTP sincronico con la URL del Balanceador como base
     */
    @Bean(name = "balanceadorRestClient")
    public RestClient balanceadorRestClient() {
        return RestClient.builder()
                .baseUrl(balanceadorUrl)
                .build();
    }
}
