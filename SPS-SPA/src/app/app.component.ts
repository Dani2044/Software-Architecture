import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';

/**
 * Componente raiz de la aplicacion SPS (Sistema de Compra de Planes de Salud).
 *
 * Renderiza la cabecera principal con la barra de navegacion que enlaza a las
 * secciones de Login, Catalogo y Carrito, y aloja el `<router-outlet>` donde
 * Angular inyecta las vistas de cada ruta.
 *
 * Este componente es standalone y no contiene logica de negocio; su unica
 * responsabilidad es definir la estructura visual de nivel superior.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header style="background:#1976d2;color:white;padding:1rem 2rem;display:flex;justify-content:space-between;">
      <strong>SPS · Sistema de Compra de Planes de Salud</strong>
      <nav style="display:flex;gap:1rem;">
        <a routerLink="/login" style="color:white;text-decoration:none;">Login</a>
        <a routerLink="/catalogo" style="color:white;text-decoration:none;">Catalogo</a>
        <a routerLink="/carrito" style="color:white;text-decoration:none;">Carrito</a>
      </nav>
    </header>
    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {}
