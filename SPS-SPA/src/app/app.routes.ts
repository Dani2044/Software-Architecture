import { Routes } from '@angular/router';

/**
 * Definicion de rutas de la aplicacion SPS.
 *
 * Todas las rutas utilizan carga diferida (`loadComponent`) para que cada
 * pagina se descargue bajo demanda, reduciendo el tamano del bundle inicial.
 *
 * | Ruta                          | Componente            | Descripcion                                      |
 * |-------------------------------|-----------------------|--------------------------------------------------|
 * | `/`                           | —                     | Redirige automaticamente a `/login`.             |
 * | `/login`                      | `PantallaAuthW`       | Formulario de inicio de sesion (JWT).            |
 * | `/catalogo`                   | `PantallaCatalogoW`   | Listado de planes de salud disponibles.          |
 * | `/carrito`                    | `PantallaCompraW`     | Resumen y confirmacion de compra.                |
 * | `/esperando/:numeroCompra`    | `EsperandoComponent`  | Pantalla de espera con polling de estado.         |
 */
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/auth/pantalla-auth-w.component').then(m => m.PantallaAuthW) },
  { path: 'catalogo', loadComponent: () => import('./pages/catalogo/pantalla-catalogo-w.component').then(m => m.PantallaCatalogoW) },
  { path: 'carrito', loadComponent: () => import('./pages/compra/pantalla-compra-w.component').then(m => m.PantallaCompraW) },
  { path: 'esperando/:numeroCompra', loadComponent: () => import('./pages/esperando/esperando.component').then(m => m.EsperandoComponent) }
];
