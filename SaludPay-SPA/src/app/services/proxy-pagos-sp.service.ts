import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Proxy HTTP para la comunicación con el backend SaludPay-Back (.NET 8).
 *
 * Encapsula el {@link HttpClient} de Angular y expone métodos genéricos
 * `get` y `post` que anteponen automáticamente la URL base del backend
 * (`http://10.32.100.111:5000`) a cada petición.
 *
 * Este servicio actúa como capa de abstracción (patrón Proxy) entre los
 * servicios de dominio ({@link EventosLoginSP}, {@link EventosTransaccion})
 * y el transporte HTTP, facilitando el cambio de URL o la adición de
 * interceptores sin modificar la lógica de negocio.
 *
 * @remarks
 * Registrado con `providedIn: 'root'` para inyección singleton.
 */
@Injectable({ providedIn: 'root' })
export class ProxyPagosSP {
  /** URL base del backend SaludPay-Back. */
  private readonly baseUrl = environment.saludPayUrl;

  /**
   * Crea una instancia del proxy.
   * @param http - Cliente HTTP de Angular utilizado para las peticiones REST.
   */
  constructor(private http: HttpClient) {}

  /**
   * Realiza una petición GET al backend.
   *
   * @typeParam T - Tipo esperado en la respuesta.
   * @param path - Ruta relativa del endpoint (e.g. `/api/compras/123`).
   * @returns Un {@link Observable} que emite la respuesta tipada.
   */
  get<T>(path: string): Observable<T> {
    return this.http.get<T>(`${this.baseUrl}${path}`);
  }

  /**
   * Realiza una petición POST al backend.
   *
   * @typeParam T - Tipo esperado en la respuesta.
   * @param path - Ruta relativa del endpoint (e.g. `/api/pago`).
   * @param body - Cuerpo de la petición.
   * @returns Un {@link Observable} que emite la respuesta tipada.
   */
  post<T>(path: string, body: any): Observable<T> {
    return this.http.post<T>(`${this.baseUrl}${path}`, body);
  }
}
