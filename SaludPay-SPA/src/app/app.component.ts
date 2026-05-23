import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Componente raíz de la aplicación SaludPay-SPA.
 *
 * Renderiza la estructura principal de la página:
 * - Un encabezado (`header`) verde con el nombre de la marca «SaludPay».
 * - Un contenedor `<main>` con un `<router-outlet>` que carga de forma
 *   dinámica las vistas de login y pago según la ruta activa.
 *
 * @remarks
 * Este componente es standalone y utiliza Angular 17 con la nueva sintaxis
 * de control de flujo. No contiene lógica de negocio; su único propósito
 * es servir como shell visual de la SPA.
 *
 * @selector sp-root
 */
@Component({
  selector: 'sp-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <header style="background:#16a34a;color:white;padding:1rem 2rem;">
      <strong>SaludPay</strong> · Pagos seguros para tus compras SPS
    </header>
    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {}
