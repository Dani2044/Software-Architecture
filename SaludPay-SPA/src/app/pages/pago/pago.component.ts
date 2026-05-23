import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CompraPendiente, SaludPayService } from '../../services/saludpay.service';

/**
 * Componente de pagos de SaludPay.
 *
 * Vista principal después del login. Muestra al usuario autenticado la lista
 * de sus compras pendientes y permite pagarlas individualmente. Cada pago
 * se procesa a través de {@link SaludPayService.pagar}, que envía la solicitud
 * al backend .NET; este publica un mensaje en la cola ActiveMQ `ColaPagoConfirmado`
 * para que el micro-servicio SAM envíe un correo de confirmación.
 *
 * @remarks
 * - Implementa `OnInit` para verificar la sesión y cargar datos al iniciar.
 * - Si no existe una sesión activa (cédula en `localStorage`), redirige
 *   automáticamente a `/login`.
 * - Utiliza la nueva sintaxis de control de flujo de Angular 17 (`@if`, `@for`).
 *
 * @selector sp-pago
 */
@Component({
  selector: 'sp-pago',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h2>Hola {{ nombre }}</h2>
      <p>Compras pendientes por pagar:</p>
      @if (cargando) { <p>Cargando...</p> }
      @if (!cargando && pendientes.length === 0) {
        <p>No tienes compras pendientes.</p>
      }
      @for (c of pendientes; track c.numeroCompra) {
        <div class="card" style="background:#f9fafb;">
          <p><strong>Compra #{{ c.numeroCompra }}</strong></p>
          <p>Valor: $ {{ c.valor | number }}</p>
          <button (click)="pagar(c)" [disabled]="pagando">Pagar</button>
        </div>
      }
      @if (mensaje) { <p style="color:#16a34a;">{{ mensaje }}</p> }
      @if (error) { <p style="color:#c00;">{{ error }}</p> }
    </div>
  `
})
export class PagoComponent implements OnInit {
  /** Lista de compras pendientes obtenidas del backend. */
  pendientes: CompraPendiente[] = [];

  /** Indica si se está cargando la lista de compras desde el servidor. */
  cargando = false;

  /** Indica si hay un pago en curso; deshabilita los botones de pago para evitar doble envío. */
  pagando = false;

  /** Mensaje de éxito mostrado al usuario tras un pago exitoso. */
  mensaje = '';

  /** Mensaje de error mostrado al usuario cuando una operación falla. */
  error = '';

  /** Nombre del usuario autenticado, obtenido de la sesión en `localStorage`. */
  nombre = '';

  /**
   * Crea una instancia del componente de pagos.
   * @param service - Servicio de SaludPay para consultar compras y procesar pagos.
   * @param router - Router de Angular para navegación programática.
   */
  constructor(private service: SaludPayService, private router: Router) {}

  /**
   * Hook de inicialización del componente.
   *
   * Verifica que exista una sesión activa (cédula en `localStorage`).
   * Si no la hay, redirige al login. De lo contrario, carga el nombre
   * del usuario y solicita la lista de compras pendientes.
   */
  ngOnInit(): void {
    if (!this.service.cedula) { this.router.navigate(['/login']); return; }
    this.nombre = this.service.nombre || '';
    this.recargar();
  }

  /**
   * Carga (o recarga) la lista de compras pendientes desde el backend.
   *
   * Activa el indicador de carga mientras la petición está en curso.
   * En caso de error, muestra un mensaje al usuario.
   */
  recargar(): void {
    this.cargando = true;
    this.service.listarPendientes(this.service.cedula!).subscribe({
      next: list => { this.pendientes = list; this.cargando = false; },
      error: () => { this.error = 'No se pudieron cargar las compras'; this.cargando = false; }
    });
  }

  /**
   * Procesa el pago de una compra pendiente.
   *
   * Deshabilita los botones de pago durante la transacción para evitar
   * doble envío. Si el pago es exitoso, muestra un mensaje de confirmación
   * y recarga la lista de compras pendientes. El backend publica un mensaje
   * en la cola ActiveMQ `ColaPagoConfirmado` para que SAM envíe el correo de confirmación.
   *
   * @param c - Objeto {@link CompraPendiente} que representa la compra a pagar.
   */
  pagar(c: CompraPendiente): void {
    this.pagando = true;
    this.mensaje = '';
    this.error = '';
    this.service.pagar(this.service.cedula!, c.numeroCompra, c.valor).subscribe({
      next: () => {
        this.mensaje = `Pago de la compra #${c.numeroCompra} registrado. Recibiras un correo de confirmacion.`;
        this.pagando = false;
        this.recargar();
      },
      error: () => { this.error = 'Error al procesar el pago'; this.pagando = false; }
    });
  }
}
