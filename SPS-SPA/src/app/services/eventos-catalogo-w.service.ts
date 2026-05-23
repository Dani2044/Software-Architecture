import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProxyWeb } from './proxy-web.service';

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
 * Servicio de eventos del catalogo de planes de la capa web.
 *
 * Responsable de obtener la lista de planes de salud disponibles
 * desde el microservicio MS-Auth-Catalogo ({@link environment.authCatalogoUrl})
 * a traves del {@link ProxyWeb}.
 */
@Injectable({ providedIn: 'root' })
export class EventosCatalogoW {
  /**
   * @param proxy - Proxy HTTP generico para realizar peticiones al backend.
   */
  constructor(private proxy: ProxyWeb) {}

  /**
   * Obtiene la lista completa de planes de salud disponibles en el catalogo.
   *
   * Realiza una peticion GET al endpoint `/api/catalogo/planes` del servicio
   * de autenticacion y catalogo.
   *
   * @returns Observable que emite un arreglo de {@link Plan} con los planes disponibles.
   */
  listarPlanes(): Observable<Plan[]> {
    return this.proxy.get<Plan[]>(`${environment.authCatalogoUrl}/api/catalogo/planes`);
  }
}
