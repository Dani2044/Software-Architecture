import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Proxy HTTP generico para la capa web de la SPA.
 *
 * Encapsula el `HttpClient` de Angular y ofrece metodos genericos `get` y `post`
 * que agregan automaticamente la URL base correspondiente al servicio backend
 * solicitado. Esto centraliza la logica de construccion de URLs y facilita su
 * modificacion sin afectar los servicios de nivel superior.
 *
 * Los servicios {@link EventosAuthW}, {@link EventosCatalogoW} y {@link EventosUIW}
 * utilizan este proxy en lugar de inyectar `HttpClient` directamente.
 */
@Injectable({ providedIn: 'root' })
export class ProxyWeb {
  /**
   * @param http - Cliente HTTP de Angular para realizar peticiones al backend.
   */
  constructor(private http: HttpClient) {}

  /**
   * Realiza una peticion GET a la URL indicada.
   *
   * @typeParam T - Tipo de la respuesta esperada.
   * @param url - URL completa del endpoint (incluyendo base URL).
   * @returns Observable que emite la respuesta tipada del servidor.
   */
  get<T>(url: string): Observable<T> {
    return this.http.get<T>(url);
  }

  /**
   * Realiza una peticion POST a la URL indicada con el cuerpo proporcionado.
   *
   * @typeParam T - Tipo de la respuesta esperada.
   * @param url - URL completa del endpoint (incluyendo base URL).
   * @param body - Cuerpo de la peticion HTTP.
   * @returns Observable que emite la respuesta tipada del servidor.
   */
  post<T>(url: string, body: any): Observable<T> {
    return this.http.post<T>(url, body);
  }
}
