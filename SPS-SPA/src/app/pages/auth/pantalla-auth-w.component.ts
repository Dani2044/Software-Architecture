import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EventosAuthW } from '../../services/eventos-auth-w.service';

/**
 * Pantalla de autenticacion de la aplicacion SPS.
 *
 * Presenta un formulario con campos de usuario y contrasena. Al enviar el
 * formulario, delega la autenticacion al {@link EventosAuthW} que valida las
 * credenciales contra el microservicio de autenticacion via JWT.
 *
 * - En caso de exito, redirige al usuario a la pagina de catalogo (`/catalogo`).
 * - En caso de error, muestra un mensaje descriptivo en pantalla.
 *
 * Este componente es standalone y utiliza `FormsModule` para el binding
 * bidireccional con `ngModel`.
 */
@Component({
  selector: 'app-pantalla-auth-w',
  standalone: true,
  imports: [FormsModule, CommonModule],
  template: `
    <div class="card" style="max-width:400px;margin:2rem auto;">
      <h2>Iniciar sesion</h2>
      <form (submit)="onSubmit($event)" style="display:flex;flex-direction:column;gap:1rem;">
        <input [(ngModel)]="username" name="username" placeholder="Usuario" required />
        <input [(ngModel)]="password" name="password" type="password" placeholder="Contrasena" required />
        <button type="submit">Entrar</button>
        @if (error) { <p style="color:#c00;">{{ error }}</p> }
      </form>
      <small>Usuario seed: <code>juan / juan123</code></small>
    </div>
  `
})
export class PantallaAuthW {
  /** Nombre de usuario ingresado en el formulario. */
  username = '';

  /** Contrasena ingresada en el formulario. */
  password = '';

  /** Mensaje de error mostrado al usuario cuando la autenticacion falla. Vacio si no hay error. */
  error = '';

  /**
   * @param auth - Servicio de eventos de autenticacion para validar credenciales y gestionar la sesion.
   * @param router - Router de Angular para redirigir tras un login exitoso.
   */
  constructor(private auth: EventosAuthW, private router: Router) {}

  /**
   * Maneja el envio del formulario de login.
   *
   * Previene el comportamiento por defecto del formulario, limpia errores previos
   * y solicita la autenticacion al servicio de auth. Si las credenciales son
   * validas, navega a `/catalogo`; de lo contrario, muestra el mensaje de error
   * retornado por el backend.
   *
   * @param e - Evento nativo del formulario, utilizado para prevenir el submit por defecto.
   */
  onSubmit(e: Event) {
    e.preventDefault();
    this.error = '';
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/catalogo']),
      error: err => this.error = err.error?.error || 'Credenciales invalidas'
    });
  }
}
