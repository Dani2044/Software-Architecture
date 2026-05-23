import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EventosCatalogoW, Plan } from '../../services/eventos-catalogo-w.service';
import { EventosUIW } from '../../services/eventos-ui-w.service';

/**
 * Pantalla del catalogo de planes de salud.
 *
 * Al inicializarse, consulta el {@link EventosCatalogoW} para obtener la lista
 * completa de planes disponibles y los presenta en un grid responsivo. Cada
 * tarjeta de plan muestra su nombre, descripcion, precio y servicios incluidos,
 * junto con un boton para agregarlo al carrito de compras.
 *
 * El carrito se gestiona a traves de {@link EventosUIW}, que persiste los datos
 * en `localStorage` bajo la clave `sps.carrito`, evitando duplicados por codigo
 * de plan.
 */
@Component({
  selector: 'app-pantalla-catalogo-w',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Catalogo de planes</h2>
    @if (loading) { <p>Cargando...</p> }
    @if (error) { <p style="color:#c00;">{{ error }}</p> }
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:1rem;">
      @for (p of planes; track p.codigo) {
        <div class="card">
          <h3>{{ p.nombre }}</h3>
          <p>{{ p.descripcion }}</p>
          <p><strong>$ {{ p.precio | number }}</strong></p>
          <ul>
            @for (s of p.servicios; track s.codigo) {
              <li>{{ s.nombre }}</li>
            }
          </ul>
          <button (click)="agregar(p)">Agregar al carrito</button>
        </div>
      }
    </div>
  `
})
export class PantallaCatalogoW implements OnInit {
  /** Lista de planes de salud obtenidos del catalogo. */
  planes: Plan[] = [];

  /** Indica si la peticion de carga del catalogo esta en curso. */
  loading = false;

  /** Mensaje de error mostrado cuando falla la carga del catalogo. Vacio si no hay error. */
  error = '';

  /**
   * @param catalogo - Servicio de eventos del catalogo para consultar los planes disponibles.
   * @param uiService - Servicio de eventos de UI para gestionar el carrito de compras.
   * @param router - Router de Angular para navegar al carrito tras agregar un plan.
   */
  constructor(
    private catalogo: EventosCatalogoW,
    private uiService: EventosUIW,
    private router: Router
  ) {}

  /**
   * Inicializa el componente cargando la lista de planes desde el backend.
   *
   * Activa el indicador de carga, realiza la peticion HTTP y, al recibir
   * la respuesta, puebla el arreglo {@link planes}. Si ocurre un error,
   * muestra un mensaje informativo al usuario.
   */
  ngOnInit(): void {
    this.loading = true;
    this.catalogo.listarPlanes().subscribe({
      next: p => { this.planes = p; this.loading = false; },
      error: err => { this.error = 'No se pudo cargar el catalogo'; this.loading = false; }
    });
  }

  /**
   * Agrega un plan de salud al carrito de compras.
   *
   * Delega al {@link EventosUIW} la persistencia del plan en `localStorage`
   * y redirige al usuario a la pagina del carrito.
   *
   * @param p - Plan de salud a agregar al carrito.
   */
  agregar(p: Plan): void {
    this.uiService.agregarAlCarrito(p);
    this.router.navigate(['/carrito']);
  }
}
