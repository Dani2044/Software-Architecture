import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EventosLoginSP } from '../../services/eventos-login-sp.service';

/**
 * Componente de inicio de sesión de SaludPay.
 *
 * Presenta un formulario donde el usuario ingresa su cédula (documento de
 * identidad colombiano) y contraseña. Al enviar el formulario se invoca
 * {@link EventosLoginSP.login}; si la autenticación es exitosa, el usuario
 * es redirigido a la vista de transacciones (`/pago`). En caso de error, se muestra
 * un mensaje descriptivo debajo del formulario.
 *
 * @remarks
 * - Componente standalone de Angular 17 con `FormsModule` para two-way binding.
 * - Las credenciales de prueba (seed) se muestran al pie del formulario:
 *   cédula `1000000001` / contraseña `juan123`.
 *
 * @selector app-pantalla-login-sp
 */
@Component({
  selector: 'app-pantalla-login-sp',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="card" style="max-width:400px;margin:2rem auto;">
      <h2>SaludPay - Identificate</h2>
      <form (submit)="onSubmit($event)" style="display:flex;flex-direction:column;gap:1rem;">
        <input [(ngModel)]="cedula" name="cedula" placeholder="Cedula" required />
        <input [(ngModel)]="password" name="password" type="password" placeholder="Contrasena" required />
        <button type="submit">Entrar</button>
        @if (error) { <p style="color:#c00;">{{ error }}</p> }
      </form>
      <small>Seed: <code>1000000001 / juan123</code></small>
    </div>
  `
})
export class PantallaLoginSP {
  /** Número de cédula ingresado por el usuario (enlazado al input con ngModel). */
  cedula = '';

  /** Contraseña ingresada por el usuario (enlazada al input con ngModel). */
  password = '';

  /** Mensaje de error mostrado al usuario cuando la autenticación falla. */
  error = '';

  /**
   * Crea una instancia del componente de login.
   * @param service - Servicio de eventos de login para autenticación y gestión de sesión.
   * @param router - Router de Angular para navegación programática.
   */
  constructor(private service: EventosLoginSP, private router: Router) {}

  /**
   * Maneja el envío del formulario de login.
   *
   * Previene el comportamiento por defecto del formulario, limpia errores
   * previos y llama a {@link EventosLoginSP.login}. Si la autenticación
   * es exitosa, navega a `/pago`. Si falla, muestra el mensaje de error
   * retornado por el backend o un mensaje genérico.
   *
   * @param e - Evento nativo del formulario (`submit`).
   */
  onSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.service.login(this.cedula, this.password).subscribe({
      next: () => this.router.navigate(['/pago']),
      error: err => this.error = err.error?.error || 'Credenciales invalidas'
    });
  }
}
