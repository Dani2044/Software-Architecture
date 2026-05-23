import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CompraService, Plan } from '../../services/compra.service';

/**
 * Componente del catalogo de planes de salud.
 *
 * Al inicializarse, consulta el {@link CompraService} para obtener la lista
 * completa de planes disponibles y los presenta en un grid responsivo. Cada
 * tarjeta de plan muestra su nombre, descripcion, precio y servicios incluidos,
 * junto con un boton para agregarlo al carrito de compras.
 *
 * El carrito se persiste en `localStorage` bajo la clave `sps.carrito`,
 * evitando duplicados por codigo de plan.
 */
@Component({
  selector: 'app-catalogo',
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
export class CatalogoComponent implements OnInit {
  /** Lista de planes de salud obtenidos del catalogo. */
  planes: Plan[] = [];

  /** Indica si la peticion de carga del catalogo esta en curso. */
  loading = false;

  /** Mensaje de error mostrado cuando falla la carga del catalogo. Vacio si no hay error. */
  error = '';

  /**
   * @param service - Servicio de compras para consultar el catalogo de planes.
   * @param router - Router de Angular para navegar al carrito tras agregar un plan.
   */
  constructor(private service: CompraService, private router: Router) {}

  /**
   * Inicializa el componente cargando la lista de planes desde el backend.
   *
   * Activa el indicador de carga, realiza la peticion HTTP y, al recibir
   * la respuesta, puebla el arreglo {@link planes}. Si ocurre un error,
   * muestra un mensaje informativo al usuario.
   */
  ngOnInit(): void {
    this.loading = true;
    this.service.listarPlanes().subscribe({
      next: p => { this.planes = p; this.loading = false; },
      error: err => { this.error = 'No se pudo cargar el catalogo'; this.loading = false; }
    });
  }

  /**
   * Agrega un plan de salud al carrito de compras.
   *
   * Lee el carrito actual desde `localStorage`, verifica que el plan no
   * exista ya (comparando por {@link Plan.codigo}), lo agrega si es nuevo
   * y redirige al usuario a la pagina del carrito.
   *
   * @param p - Plan de salud a agregar al carrito.
   */
  agregar(p: Plan): void {
    const carrito: Plan[] = JSON.parse(localStorage.getItem('sps.carrito') || '[]');
    if (!carrito.find(x => x.codigo === p.codigo)) carrito.push(p);
    localStorage.setItem('sps.carrito', JSON.stringify(carrito));
    this.router.navigate(['/carrito']);
  }
}
