/**
 * Configuracion de entorno para desarrollo local.
 *
 * Define las URLs base de los servicios backend con los que se comunica la SPA:
 * - {@link environment.balanceadorUrl} — Balanceador de carga que distribuye las
 *   peticiones de compra entre las instancias disponibles de MS-Compra.
 * - {@link environment.authCatalogoUrl} — Servicio de autenticacion (JWT) y
 *   catalogo de planes de salud, accedido directamente durante desarrollo.
 */
export const environment = {
  /** Indica si la aplicacion se ejecuta en modo produccion. */
  production: false,

  /**
   * URL del balanceador de carga (puerto 8080).
   * Todas las operaciones de compra se enrutan a traves de este endpoint.
   */
  balanceadorUrl: 'http://10.43.100.122:8080',

  /**
   * URL directa al microservicio de autenticacion y catalogo (puerto 8082).
   * Se usa para login JWT y para obtener la lista de planes de salud.
   */
  authCatalogoUrl: 'http://10.43.100.122:8082'
};
