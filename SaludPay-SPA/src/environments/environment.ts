/**
 * Configuración de entorno de desarrollo para SaludPay-SPA.
 *
 * Define las variables de entorno utilizadas en toda la aplicación,
 * incluyendo la URL base del backend .NET (SaludPay-Back) que expone
 * los endpoints de autenticación, compras pendientes y procesamiento de pagos.
 *
 * @remarks
 * En producción se debe reemplazar `saludPayUrl` por la URL del servidor
 * desplegado y establecer `production` en `true`.
 */
export const environment = {
  /** Indica si la aplicación se ejecuta en modo producción. */
  production: false,

  /**
   * URL base del backend SaludPay-Back (.NET 8) que escucha en el puerto 5000.
   * Todos los llamados HTTP del {@link SaludPayService} se construyen a partir de esta URL.
   */
  saludPayUrl: 'http://10.43.101.18:5000'
};
