import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

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
 * Servicio principal de SaludPay-SPA.
 *
 * Encapsula toda la comunicación HTTP con el backend SaludPay-Back (.NET 8,
 * puerto 5000) y gestiona la sesión del usuario en `localStorage`.
 *
 * Flujo típico:
 * 1. El usuario se autentica con {@link login} proporcionando cédula y contraseña.
 * 2. Se listan sus compras pendientes con {@link listarPendientes}.
 * 3. Al presionar «Pagar», se invoca {@link pagar}, que registra el pago en el
 *    backend y este a su vez envía un mensaje a la cola ActiveMQ `ColaPagoConfirmado`
 *    para notificar al sistema SAM (envío de correo de confirmación).
 *
 * @remarks
 * Registrado con `providedIn: 'root'` para que Angular lo inyecte como singleton.
 */
@Injectable({ providedIn: 'root' })
export class SaludPayService {
  /**
   * Crea una instancia del servicio.
   * @param http - Cliente HTTP de Angular utilizado para las peticiones REST.
   */
  constructor(private http: HttpClient) {}

  /**
   * Autentica al usuario contra el endpoint `/api/saludpay/auth/login`.
   *
   * Si las credenciales son válidas, almacena la cédula y el nombre del
   * usuario en `localStorage` bajo las claves `sp.cedula` y `sp.nombre`
   * para mantener la sesión entre recargas de página.
   *
   * @param cedula - Número de cédula (documento de identidad) del paciente.
   * @param password - Contraseña del paciente.
   * @returns Un {@link Observable} que emite un objeto con `cedula` y `nombre`
   *          del usuario autenticado.
   */
  login(cedula: string, password: string): Observable<{ cedula: string; nombre: string }> {
    return this.http
      .post<{ cedula: string; nombre: string }>(`${environment.saludPayUrl}/api/saludpay/auth/login`, { cedula, password })
      .pipe(tap(res => {
        localStorage.setItem('sp.cedula', res.cedula);
        localStorage.setItem('sp.nombre', res.nombre);
      }));
  }

  /**
   * Obtiene la lista de compras pendientes de pago para un paciente.
   *
   * Consulta el endpoint `GET /api/compras/{cedula}` del backend.
   *
   * @param cedula - Cédula del paciente cuyas compras pendientes se desean consultar.
   * @returns Un {@link Observable} que emite un arreglo de {@link CompraPendiente}.
   */
  listarPendientes(cedula: string): Observable<CompraPendiente[]> {
    return this.http.get<CompraPendiente[]>(`${environment.saludPayUrl}/api/compras/${cedula}`);
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
    return this.http.post(`${environment.saludPayUrl}/api/pago`, { cedula, numeroCompra, valorPagado });
  }

  /**
   * Cédula del usuario actualmente autenticado, leída desde `localStorage`.
   * @returns La cédula almacenada o `null` si no hay sesión activa.
   */
  get cedula(): string | null { return localStorage.getItem('sp.cedula'); }

  /**
   * Nombre del usuario actualmente autenticado, leído desde `localStorage`.
   * @returns El nombre almacenado o `null` si no hay sesión activa.
   */
  get nombre(): string | null { return localStorage.getItem('sp.nombre'); }

  /**
   * Cierra la sesión del usuario eliminando todos los datos de `localStorage`.
   */
  logout(): void { localStorage.clear(); }
}
