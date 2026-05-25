import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProxyWeb } from './proxy-web.service';
import { Plan } from './eventos-catalogo-w.service';

/**
 * Payload enviado al backend para crear una nueva compra.
 *
 * Contiene los datos del cliente autenticado y la lista de planes
 * seleccionados con sus respectivos servicios medicos.
 */
export interface CrearCompraRequest {
  /** Cedula (documento de identidad) del cliente comprador. */
  cedulaCliente: string;

  /** Nombre completo del cliente. */
  nombreCliente: string;

  /** Correo electronico del cliente para notificaciones y link de pago. */
  correoCliente: string;

  /**
   * Lista de planes seleccionados para la compra.
   * Cada elemento contiene los datos esenciales del plan y sus servicios.
   */
  planes: {
    /** Codigo del plan. */
    codigo: string;
    /** Nombre del plan. */
    nombre: string;
    /** Precio del plan. */
    precio: number;
    /** Servicios incluidos en el plan. */
    servicios: { codigo: string; nombre: string; duracionMinutos?: number }[];
  }[];
}

/**
 * Servicio de eventos de interfaz de usuario de la capa web.
 *
 * Gestiona el estado del carrito de compras en `localStorage` y se comunica
 * con {@link environment.authCatalogoUrl} (MS-Auth-Catalogo) a traves del
 * {@link ProxyWeb} para crear y consultar compras. Es {@code ProxyCatalogo}
 * dentro de MS-Auth-Catalogo el que reenvia internamente las peticiones de
 * compra al Balanceador del sistema (round-robin sobre MS-Compra).
 *
 * Ofrece tres operaciones principales:
 * 1. {@link crearCompra} — Envia la solicitud de compra a ProxyCatalogo.
 * 2. {@link consultarEstado} — Consulta el estado actual de una compra existente.
 * 3. Gestion del carrito — {@link obtenerCarrito}, {@link agregarAlCarrito},
 *    {@link limpiarCarrito}.
 */
@Injectable({ providedIn: 'root' })
export class EventosUIW {
  /**
   * @param proxy - Proxy HTTP generico para realizar peticiones al backend.
   */
  constructor(private proxy: ProxyWeb) {}

  // ── Carrito (localStorage) ───────────────────────────────────────────

  /**
   * Obtiene la lista de planes actualmente en el carrito.
   *
   * @returns Arreglo de {@link Plan} almacenados en `localStorage`.
   */
  obtenerCarrito(): Plan[] {
    return JSON.parse(localStorage.getItem('sps.carrito') || '[]');
  }

  /**
   * Agrega un plan al carrito de compras, evitando duplicados por codigo.
   *
   * @param plan - Plan de salud a agregar.
   */
  agregarAlCarrito(plan: Plan): void {
    const carrito = this.obtenerCarrito();
    if (!carrito.find(x => x.codigo === plan.codigo)) carrito.push(plan);
    localStorage.setItem('sps.carrito', JSON.stringify(carrito));
  }

  /**
   * Elimina todos los planes del carrito.
   */
  limpiarCarrito(): void {
    localStorage.removeItem('sps.carrito');
  }

  // ── Compra (HTTP via ProxyWeb) ───────────────────────────────────────

  /**
   * Crea una nueva solicitud de compra.
   *
   * La peticion se envia a {@code ProxyCatalogo} en MS-Auth-Catalogo, que la
   * reenvia al Balanceador. El Balanceador a su vez distribuye la peticion a
   * una de las instancias activas de MS-Compra mediante round-robin. La compra
   * inicia en estado `CREADA` y posteriormente sera procesada por la SNS
   * (Superintendencia Nacional de Salud).
   *
   * @param req - Datos de la compra incluyendo informacion del cliente y planes seleccionados.
   * @returns Observable con el numero de compra asignado, el estado inicial y un mensaje descriptivo.
   */
  crearCompra(req: CrearCompraRequest): Observable<{ numeroCompra: number; estado: string; mensaje: string }> {
    return this.proxy.post<{ numeroCompra: number; estado: string; mensaje: string }>(
      `${environment.authCatalogoUrl}/api/compra`, req
    );
  }

  /**
   * Consulta el estado actual de una compra existente.
   *
   * Realiza una peticion GET a ProxyCatalogo (MS-Auth-Catalogo), que la
   * reenvia al Balanceador. Los estados posibles incluyen: `CREADA`, `APROBADA`,
   * `RECHAZADA`, entre otros.
   *
   * @param numeroCompra - Numero unico de la compra a consultar.
   * @returns Observable con los datos actualizados de la compra.
   */
  consultarEstado(numeroCompra: number): Observable<any> {
    return this.proxy.get(`${environment.authCatalogoUrl}/api/compra/${numeroCompra}`);
  }
}
