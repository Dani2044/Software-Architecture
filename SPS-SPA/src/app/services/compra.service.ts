import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

/**
 * Representa un servicio medico incluido dentro de un plan de salud.
 *
 * Cada plan puede contener uno o mas servicios medicos (consultas,
 * procedimientos, examenes, etc.) que el afiliado puede utilizar.
 */
export interface ServicioMedico {
  /** Codigo unico que identifica el servicio medico. */
  codigo: string;

  /** Nombre descriptivo del servicio (ej. "Consulta general", "Radiografia"). */
  nombre: string;

  /** Duracion estimada del servicio en minutos. Opcional segun el tipo de servicio. */
  duracionMinutos?: number;

  /** Precio individual del servicio medico. Puede no estar presente si se factura como parte del plan. */
  precio?: number;
}

/**
 * Representa un plan de salud disponible en el catalogo.
 *
 * Un plan agrupa un conjunto de {@link ServicioMedico} bajo un precio unico
 * que el cliente puede adquirir a traves de la plataforma SPS.
 */
export interface Plan {
  /** Codigo unico del plan de salud (ej. "PLAN-001"). */
  codigo: string;

  /** Nombre comercial del plan (ej. "Plan Basico Familiar"). */
  nombre: string;

  /** Precio total del plan de salud en la moneda local. */
  precio: number;

  /** Descripcion detallada del plan y sus beneficios. */
  descripcion?: string;

  /** Lista de servicios medicos incluidos en el plan. */
  servicios: ServicioMedico[];
}

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
 * Servicio encargado de la gestion de compras y consulta del catalogo de planes.
 *
 * Ofrece tres operaciones principales:
 * 1. {@link listarPlanes} — Obtiene el catalogo completo de planes desde
 *    MS-Auth-Catalogo ({@link environment.authCatalogoUrl}).
 * 2. {@link crearCompra} — Envia la solicitud de compra al balanceador de carga
 *    ({@link environment.balanceadorUrl}), que la distribuye a una instancia de MS-Compra.
 * 3. {@link consultarEstado} — Consulta el estado actual de una compra existente
 *    a traves del balanceador.
 */
@Injectable({ providedIn: 'root' })
export class CompraService {
  /**
   * @param http - Cliente HTTP de Angular para realizar peticiones al backend.
   */
  constructor(private http: HttpClient) {}

  /**
   * Obtiene la lista completa de planes de salud disponibles en el catalogo.
   *
   * Realiza una peticion GET al endpoint `/api/catalogo/planes` del servicio
   * de autenticacion y catalogo.
   *
   * @returns Observable que emite un arreglo de {@link Plan} con los planes disponibles.
   */
  listarPlanes(): Observable<Plan[]> {
    return this.http.get<Plan[]>(`${environment.authCatalogoUrl}/api/catalogo/planes`);
  }

  /**
   * Crea una nueva solicitud de compra enviandola al balanceador de carga.
   *
   * El balanceador distribuye la peticion a una de las instancias activas de
   * MS-Compra mediante round-robin. La compra inicia en estado `CREADA` y
   * posteriormente sera procesada por la SNS (Superintendencia Nacional de Salud).
   *
   * @param req - Datos de la compra incluyendo informacion del cliente y planes seleccionados.
   * @returns Observable con el numero de compra asignado, el estado inicial y un mensaje descriptivo.
   */
  crearCompra(req: CrearCompraRequest): Observable<{ numeroCompra: number; estado: string; mensaje: string }> {
    // Pasa por el balanceador
    return this.http.post<{ numeroCompra: number; estado: string; mensaje: string }>(
      `${environment.balanceadorUrl}/api/compra`, req
    );
  }

  /**
   * Consulta el estado actual de una compra existente.
   *
   * Realiza una peticion GET al balanceador para obtener los detalles y el
   * estado de procesamiento de la compra identificada por su numero.
   * Los estados posibles incluyen: `CREADA`, `APROBADA`, `RECHAZADA`, entre otros.
   *
   * @param numeroCompra - Numero unico de la compra a consultar.
   * @returns Observable con los datos actualizados de la compra.
   */
  consultarEstado(numeroCompra: number): Observable<any> {
    return this.http.get(`${environment.balanceadorUrl}/api/compra/${numeroCompra}`);
  }
}
