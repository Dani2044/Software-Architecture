import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ProxyPagosSP } from './proxy-pagos-sp.service';

/**
 * Servicio de autenticación de SaludPay-SPA.
 *
 * Gestiona el inicio de sesión del paciente contra el endpoint
 * `/api/saludpay/auth/login` del backend SaludPay-Back, y mantiene
 * la sesión del usuario en `localStorage`.
 *
 * Utiliza {@link ProxyPagosSP} como capa de transporte HTTP.
 *
 * @remarks
 * Registrado con `providedIn: 'root'` para inyección singleton.
 */
@Injectable({ providedIn: 'root' })
export class EventosLoginSP {
  /**
   * Crea una instancia del servicio de login.
   * @param proxy - Proxy HTTP para la comunicación con SaludPay-Back.
   */
  constructor(private proxy: ProxyPagosSP) {}

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
    return this.proxy
      .post<{ cedula: string; nombre: string }>('/api/saludpay/auth/login', { cedula, password })
      .pipe(tap(res => {
        localStorage.setItem('sp.cedula', res.cedula);
        localStorage.setItem('sp.nombre', res.nombre);
      }));
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
