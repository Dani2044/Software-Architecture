package com.sps.authcatalogo.catalogo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Cliente outbound del modulo de catalogo que reenvia las peticiones
 * de compra al Balanceador del sistema SPS.
 *
 * <p>Alineado con el diagrama de despliegue UML, esta clase es un
 * <b>cliente HTTP</b> (no un controller REST). Su unica responsabilidad
 * es comunicarse con el {@code WSIPVirtual} del Balanceador a traves
 * de un {@link RestClient}. Es invocado por {@link SrvCatalogo} cuando
 * se necesita crear o consultar una compra.</p>
 *
 * <p>Flujo de compra completo:</p>
 * <pre>
 *   SPS-SPA (ProxyWeb)
 *        -> WSCatalogo (REST controller en este modulo)
 *        -> SrvCatalogo (logica de negocio)
 *        -> ProxyCatalogo (este componente)
 *        -> WSIPVirtual (Balanceador, otro modulo)
 *        -> MS-Compra (replicas via round-robin)
 * </pre>
 *
 * @author SPS Team
 * @see SrvCatalogo
 * @see com.sps.authcatalogo.config.WebClientConfig
 */
@Component
public class ProxyCatalogo {

    private static final Logger log = LoggerFactory.getLogger(ProxyCatalogo.class);

    private final RestClient balanceadorRestClient;

    public ProxyCatalogo(@Qualifier("balanceadorRestClient") RestClient balanceadorRestClient) {
        this.balanceadorRestClient = balanceadorRestClient;
    }

    /**
     * Reenvia un POST de creacion de compra al WSIPVirtual del Balanceador.
     *
     * @param body cuerpo de la peticion (datos de la compra)
     * @return respuesta JSON del Balanceador (numeroCompra, estado, mensaje)
     * @throws ProxyCatalogoException si el Balanceador no responde o devuelve error
     */
    public String crearCompra(Object body) {
        log.info("ProxyCatalogo -> Balanceador POST /api/compra");
        return forwardPost("/api/compra", body);
    }

    /**
     * Reenvia un POST con sub-path arbitrario al WSIPVirtual del Balanceador.
     *
     * @param subPath  sub-path adicional bajo /api/compra (ej. "/123/cancelar")
     * @param body     cuerpo de la peticion
     * @return respuesta JSON del Balanceador
     */
    public String crearCompraSubpath(String subPath, Object body) {
        String path = "/api/compra" + (subPath.startsWith("/") ? subPath : "/" + subPath);
        log.info("ProxyCatalogo -> Balanceador POST {}", path);
        return forwardPost(path, body);
    }

    /**
     * Consulta el estado de una compra existente al WSIPVirtual del Balanceador.
     *
     * @param numeroCompra identificador unico de la compra
     * @return respuesta JSON del Balanceador con el estado actual
     */
    public String consultarEstado(long numeroCompra) {
        String path = "/api/compra/" + numeroCompra;
        log.info("ProxyCatalogo -> Balanceador GET {}", path);
        return forwardGet(path);
    }

    /**
     * Reenvia un GET arbitrario al WSIPVirtual del Balanceador.
     *
     * @param fullPath path completo (con query string si aplica) bajo /api/compra
     * @return respuesta del Balanceador
     */
    public String consultarSubpath(String fullPath) {
        log.info("ProxyCatalogo -> Balanceador GET {}", fullPath);
        return forwardGet(fullPath);
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────

    private String forwardPost(String path, Object body) {
        try {
            return balanceadorRestClient.post()
                    .uri(path)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            log.warn("Balanceador respondio {}: {}", ex.getStatusCode(), ex.getMessage());
            throw new ProxyCatalogoException(ex.getStatusCode().value(),
                    ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error reenviando POST al Balanceador: {}", ex.getMessage());
            throw new ProxyCatalogoException(503, "{\"error\":\"Balanceador no disponible\"}");
        }
    }

    private String forwardGet(String path) {
        try {
            return balanceadorRestClient.get()
                    .uri(path)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException ex) {
            log.warn("Balanceador respondio {}: {}", ex.getStatusCode(), ex.getMessage());
            throw new ProxyCatalogoException(ex.getStatusCode().value(),
                    ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Error reenviando GET al Balanceador: {}", ex.getMessage());
            throw new ProxyCatalogoException(503, "{\"error\":\"Balanceador no disponible\"}");
        }
    }

    /**
     * Excepcion que encapsula el codigo HTTP y el body de error retornado
     * por el Balanceador. {@link SrvCatalogo} o el controller la pueden
     * propagar al cliente HTTP original.
     */
    public static class ProxyCatalogoException extends RuntimeException {
        private final int statusCode;
        private final String body;

        public ProxyCatalogoException(int statusCode, String body) {
            super("Balanceador HTTP " + statusCode);
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() { return statusCode; }
        public String getBody() { return body; }
    }
}
