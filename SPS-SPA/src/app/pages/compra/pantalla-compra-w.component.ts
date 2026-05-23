import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EventosAuthW } from '../../services/eventos-auth-w.service';
import { EventosUIW } from '../../services/eventos-ui-w.service';
import { Plan } from '../../services/eventos-catalogo-w.service';

/**
 * Pantalla de compra (carrito) de la aplicacion SPS.
 *
 * Muestra los planes de salud que el usuario ha seleccionado desde el catalogo,
 * calcula el precio total y permite confirmar la compra. Los planes se gestionan
 * a traves de {@link EventosUIW}.
 *
 * Al confirmar:
 * 1. Valida que el usuario este autenticado (redirige a `/login` si no lo esta).
 * 2. Construye el request con los datos del cliente y planes.
 * 3. Envia la solicitud al backend a traves del {@link EventosUIW}.
 * 4. Limpia el carrito y redirige a la pantalla de espera
 *    (`/esperando/:numeroCompra`).
 */
@Component({
  selector: 'app-pantalla-compra-w',
  standalone: true,
  imports: [CommonModule],
  template: `
    <h2>Carrito</h2>
    @if (carrito.length === 0) {
      <p>El carrito esta vacio.</p>
    } @else {
      <div class="card">
        <ul>
          @for (p of carrito; track p.codigo) {
            <li>{{ p.nombre }} - $ {{ p.precio | number }}</li>
          }
        </ul>
        <p><strong>Total: $ {{ total | number }}</strong></p>
        <button (click)="confirmar()" [disabled]="enviando">Confirmar compra</button>
        @if (error) { <p style="color:#c00;">{{ error }}</p> }
      </div>
    }
  `
})
export class PantallaCompraW implements OnInit {
  /** Lista de planes de salud actualmente en el carrito. */
  carrito: Plan[] = [];

  /** Precio total acumulado de todos los planes en el carrito. */
  total = 0;

  /** Indica si la solicitud de compra esta siendo enviada al backend. Se usa para deshabilitar el boton. */
  enviando = false;

  /** Mensaje de error mostrado cuando falla la creacion de la compra. Vacio si no hay error. */
  error = '';

  /**
   * @param auth - Servicio de eventos de autenticacion para obtener los datos del cliente.
   * @param uiService - Servicio de eventos de UI para gestionar el carrito y crear compras.
   * @param router - Router de Angular para redirigir segun el flujo de la compra.
   */
  constructor(
    private auth: EventosAuthW,
    private uiService: EventosUIW,
    private router: Router
  ) {}

  /**
   * Inicializa el componente cargando los planes del carrito desde el servicio de UI.
   *
   * Obtiene el carrito a traves de {@link EventosUIW.obtenerCarrito} y calcula
   * el precio total sumando el precio de cada plan.
   */
  ngOnInit(): void {
    this.carrito = this.uiService.obtenerCarrito();
    this.total = this.carrito.reduce((s, p) => s + (p.precio || 0), 0);
  }

  /**
   * Confirma la compra y la envia al backend a traves del balanceador de carga.
   *
   * Verifica que exista una sesion activa (cedula). Si no hay sesion, redirige
   * a `/login`. En caso contrario, construye el payload con los datos del
   * cliente y los planes seleccionados, y lo envia al servicio de compras.
   *
   * Tras una respuesta exitosa, limpia el carrito y redirige a la pantalla
   * de espera con el numero de compra asignado.
   */
  confirmar(): void {
    if (!this.auth.cedula) { this.router.navigate(['/login']); return; }
    this.enviando = true;
    this.error = '';
    this.uiService.crearCompra({
      cedulaCliente: this.auth.cedula!,
      nombreCliente: this.auth.nombre || '',
      correoCliente: this.auth.correo || '',
      planes: this.carrito.map(p => ({
        codigo: p.codigo,
        nombre: p.nombre,
        precio: p.precio,
        servicios: (p.servicios || []).map(s => ({
          codigo: s.codigo, nombre: s.nombre, duracionMinutos: s.duracionMinutos
        }))
      }))
    }).subscribe({
      next: res => {
        this.uiService.limpiarCarrito();
        this.router.navigate(['/esperando', res.numeroCompra]);
      },
      error: err => { this.error = 'Error al crear la compra'; this.enviando = false; }
    });
  }
}
