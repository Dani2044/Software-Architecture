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
 * | `/login`                      | `LoginComponent`      | Formulario de inicio de sesion (JWT).            |
 * | `/catalogo`                   | `CatalogoComponent`   | Listado de planes de salud disponibles.          |
 * | `/carrito`                    | `CarritoComponent`    | Resumen y confirmacion de compra.                |
 * | `/esperando/:numeroCompra`    | `EsperandoComponent`  | Pantalla de espera con polling de estado.         |
 */
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'catalogo', loadComponent: () => import('./pages/catalogo/catalogo.component').then(m => m.CatalogoComponent) },
  { path: 'carrito', loadComponent: () => import('./pages/carrito/carrito.component').then(m => m.CarritoComponent) },
  { path: 'esperando/:numeroCompra', loadComponent: () => import('./pages/esperando/esperando.component').then(m => m.EsperandoComponent) }
];
