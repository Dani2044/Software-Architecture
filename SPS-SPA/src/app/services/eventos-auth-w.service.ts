import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProxyWeb } from './proxy-web.service';

/**
 * Respuesta del endpoint de autenticacion `/api/auth/login`.
 *
 * Contiene el token JWT y los datos basicos del usuario autenticado,
 * que se persisten en `localStorage` para uso posterior en la sesion.
 */
export interface LoginResponse {
  /** Token JWT firmado, utilizado como `Authorization: Bearer` en peticiones subsecuentes. */
  token: string;

  /** Numero de cedula (documento de identidad) del usuario. */
  cedula: string;

  /** Nombre completo del usuario autenticado. */
  nombre: string;

  /** Correo electronico del usuario, usado para notificaciones de compra. */
  correo: string;
}

/**
 * Servicio de eventos de autenticacion de la capa web.
 *
 * Se comunica con el microservicio MS-Auth-Catalogo ({@link environment.authCatalogoUrl})
 * a traves del {@link ProxyWeb} para validar las credenciales del usuario.
 * Tras un login exitoso, almacena el token y los datos del usuario en
 * `localStorage` bajo el prefijo `sps.*`.
 *
 * Provee accesores de solo lectura para recuperar los datos de sesion y un
 * metodo {@link logout} para limpiar toda la informacion almacenada.
 *
 * @example
 * ```ts
 * this.eventosAuth.login('juan', 'juan123').subscribe({
 *   next: () => console.log('Autenticado:', this.eventosAuth.nombre),
 *   error: err => console.error(err)
 * });
 * ```
 */
@Injectable({ providedIn: 'root' })
export class EventosAuthW {
  /**
   * @param proxy - Proxy HTTP generico para realizar peticiones al backend.
   */
  constructor(private proxy: ProxyWeb) {}

  /**
   * Autentica al usuario contra el servicio de autenticacion.
   *
   * Envia las credenciales al endpoint `/api/auth/login` y, si la respuesta
   * es exitosa, almacena automaticamente el token JWT y los datos del usuario
   * en `localStorage`.
   *
   * @param username - Nombre de usuario o identificador de acceso.
   * @param password - Contrasena del usuario.
   * @returns Observable que emite un {@link LoginResponse} con el token y datos del usuario.
   */
  login(username: string, password: string): Observable<LoginResponse> {
    return this.proxy
      .post<LoginResponse>(`${environment.authCatalogoUrl}/api/auth/login`, { username, password })
      .pipe(tap(res => {
        localStorage.setItem('sps.token', res.token);
        localStorage.setItem('sps.cedula', res.cedula);
        localStorage.setItem('sps.nombre', res.nombre);
        localStorage.setItem('sps.correo', res.correo);
      }));
  }

  /** Token JWT almacenado en la sesion actual, o `null` si no hay sesion activa. */
  get token(): string | null { return localStorage.getItem('sps.token'); }

  /** Cedula del usuario autenticado, o `null` si no hay sesion activa. */
  get cedula(): string | null { return localStorage.getItem('sps.cedula'); }

  /** Nombre completo del usuario autenticado, o `null` si no hay sesion activa. */
  get nombre(): string | null { return localStorage.getItem('sps.nombre'); }

  /** Correo electronico del usuario autenticado, o `null` si no hay sesion activa. */
  get correo(): string | null { return localStorage.getItem('sps.correo'); }

  /**
   * Cierra la sesion del usuario eliminando todos los datos de `localStorage`.
   *
   * Esto incluye el token JWT, la cedula, el nombre y el correo.
   */
  logout(): void { localStorage.clear(); }
}
