import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ProxyPagosSP } from './proxy-pagos-sp.service';

/**
 * Representa una compra pendiente de pago asociada a un paciente.
 *
 * El backend (MS-Compra) genera estas compras cuando un paciente adquiere
 * productos o servicios dentro del sistema SPS. Permanecen en estado
 * `"PENDIENTE"` hasta que el usuario las paga a través de SaludPay.
 */
export interface CompraPendiente {
  /** Identificador único secuencial de la compra. */
  numeroCompra: number;

  /** Cédula (documento de identidad) del paciente propietario de la compra. */
  cedula: string;

  /** Valor total de la compra en pesos colombianos (COP). */
  valor: number;

  /** Estado actual de la compra (e.g. `"PENDIENTE"`, `"PAGADA"`). */
  estado: string;

  /** Fecha y hora de creación de la compra en formato ISO 8601. */
  fechaCreacion: string;
}

/**
 * Servicio de transacciones de SaludPay-SPA.
 *
 * Encapsula las operaciones de consulta de compras pendientes y
 * procesamiento de pagos, comunicándose con el backend SaludPay-Back
 * a través del {@link ProxyPagosSP}.
 *
 * Endpoints consumidos:
 * - `GET  /api/compras/{cedula}` — lista de compras pendientes.
 * - `POST /api/pago` — registro de un pago.
 *
 * @remarks
 * Registrado con `providedIn: 'root'` para inyección singleton.
 */
@Injectable({ providedIn: 'root' })
export class EventosTransaccion {
  /**
   * Crea una instancia del servicio de transacciones.
   * @param proxy - Proxy HTTP para la comunicación con SaludPay-Back.
   */
  constructor(private proxy: ProxyPagosSP) {}

  /**
   * Obtiene la lista de compras pendientes de pago para un paciente.
   *
   * Consulta el endpoint `GET /api/compras/{cedula}` del backend.
   *
   * @param cedula - Cédula del paciente cuyas compras pendientes se desean consultar.
   * @returns Un {@link Observable} que emite un arreglo de {@link CompraPendiente}.
   */
  listarPendientes(cedula: string): Observable<CompraPendiente[]> {
    return this.proxy.get<CompraPendiente[]>(`/api/compras/${cedula}`);
  }

  /**
   * Registra el pago de una compra pendiente.
   *
   * Envía un `POST` al endpoint `/api/pago` del backend. El backend, a su vez,
   * publica un mensaje en la cola ActiveMQ `ColaPagoConfirmado` para que el servicio
   * SAM envíe un correo electrónico de confirmación al paciente.
   *
   * @param cedula - Cédula del paciente que realiza el pago.
   * @param numeroCompra - Número identificador de la compra a pagar.
   * @param valorPagado - Monto pagado en pesos colombianos (COP).
   * @returns Un {@link Observable} con la respuesta del servidor.
   */
  pagar(cedula: string, numeroCompra: number, valorPagado: number): Observable<any> {
    return this.proxy.post('/api/pago', { cedula, numeroCompra, valorPagado });
  }
}
