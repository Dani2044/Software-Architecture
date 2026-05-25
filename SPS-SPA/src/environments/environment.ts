/**
 * Configuracion de entorno para la SPA del sistema SPS.
 *
 * <p>Segun el diagrama de despliegue UML, el SPA habla unicamente con los
 * WebServices de {@code MS-Auth-Catalogo} a traves del {@code ProxyWeb}.
 * Las operaciones de compra son reenviadas internamente por {@code ProxyCatalogo}
 * al Balanceador del sistema. La SPA no conoce la IP del Balanceador.</p>
 */
export const environment = {
  /** Indica si la aplicacion se ejecuta en modo produccion. */
  production: false,

  /**
   * URL del microservicio MS-Auth-Catalogo (puerto 8082).
   *
   * <p>Este es el unico backend con el que la SPA se comunica:</p>
   * <ul>
   *   <li>{@code /api/auth/**}    — login y registro (AuthController).</li>
   *   <li>{@code /api/catalogo/**}— planes y servicios (ProxyCatalogo, datos locales).</li>
   *   <li>{@code /api/compra/**}  — crear/consultar compras (ProxyCatalogo reenvia al Balanceador).</li>
   * </ul>
   */
  authCatalogoUrl: 'http://10.43.101.18:8082',

  /**
   * URL del SaludPay-SPA (puerto 4201) — pantalla de pago.
   * Se usa como destino del boton "Ir a pagar en SaludPay" que aparece
   * en EsperandoComponent cuando la compra es APROBADA por la SNS.
   */
  saludPayUrl: 'http://10.43.100.111:4201/pago'
};
