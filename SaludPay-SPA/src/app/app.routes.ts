import { Routes } from '@angular/router';

/**
 * Definición de rutas de la aplicación SaludPay-SPA.
 *
 * Utiliza carga diferida (lazy loading) para cada página, lo que reduce
 * el tamaño del bundle inicial y mejora el tiempo de carga.
 *
 * Rutas disponibles:
 * | Ruta      | Componente        | Descripción                                     |
 * |-----------|-------------------|-------------------------------------------------|
 * | `/`       | —                 | Redirige automáticamente a `/login`.             |
 * | `/login`  | {@link LoginComponent} | Formulario de autenticación por cédula.     |
 * | `/pago`   | {@link PagoComponent}  | Lista de compras pendientes y botón de pago.|
 *
 * @remarks
 * No se implementa un guard de autenticación a nivel de ruta; la protección
 * se maneja dentro de {@link PagoComponent.ngOnInit}, que redirige al login
 * si no existe una sesión activa en `localStorage`.
 */
export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'pago', loadComponent: () => import('./pages/pago/pago.component').then(m => m.PagoComponent) }
];
