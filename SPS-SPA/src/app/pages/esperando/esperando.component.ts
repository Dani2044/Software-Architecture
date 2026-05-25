import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { EventosUIW } from '../../services/eventos-ui-w.service';
import { environment } from '../../../environments/environment';

/**
 * Componente de espera que muestra el estado de procesamiento de una compra.
 *
 * Tras confirmar una compra, el usuario es redirigido a esta pantalla donde
 * se muestra el numero de compra y su estado actual. El componente realiza
 * polling automatico cada {@link intervalo} segundos (por defecto 5) al
 * endpoint de consulta de estado del {@link EventosUIW}.
 *
 * La compra pasa por varios estados (ej. `CREADA`, `APROBADA`, `RECHAZADA`)
 * a medida que la SNS (Superintendencia Nacional de Salud) procesa la solicitud.
 * Cuando la compra es aprobada, el sistema envia un correo con el link de pago
 * Y ADEMAS aparece un boton "Ir a pagar en SaludPay" directamente en esta pantalla
 * para que el cliente no tenga que ir al inbox del correo.
 *
 * El polling se detiene automaticamente al destruir el componente (`ngOnDestroy`)
 * o cuando la compra alcanza un estado final (APROBADA / RECHAZADA / TERMINADA),
 * limpiando el `setInterval` para evitar fugas de memoria y peticiones innecesarias.
 */
@Component({
  selector: 'app-esperando',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="card">
      <h2>Esperando confirmacion</h2>
      <p>
        Tu compra <strong>#{{ numeroCompra }}</strong> esta en proceso.
        Cuando la SNS apruebe los planes te enviaremos un correo con el link de pago.
      </p>
      <p>Estado actual: <strong>{{ estado }}</strong></p>

      <!-- Boton de pago: solo visible cuando la compra fue APROBADA por la SNS -->
      <div *ngIf="estado === 'APROBADA'" class="cta-pago">
        <p>Tu compra fue aprobada. Continua el pago en SaludPay:</p>
        <a [href]="saludPayUrl" target="_blank" rel="noopener" class="btn-pagar">
          Ir a pagar en SaludPay &rarr;
        </a>
      </div>

      <small *ngIf="!estadoFinal()">Esta pagina se refresca automaticamente cada {{ intervalo }}s.</small>
    </div>
  `,
  styles: [`
    .cta-pago {
      margin-top: 1.5rem;
      padding: 1rem;
      background: #e8f5e9;
      border-left: 4px solid #28a745;
      border-radius: 6px;
    }
    .cta-pago p {
      margin: 0 0 .75rem 0;
      font-weight: 500;
    }
    .btn-pagar {
      display: inline-block;
      background: #28a745;
      color: white;
      padding: .75rem 1.5rem;
      border-radius: 4px;
      text-decoration: none;
      font-weight: bold;
    }
    .btn-pagar:hover {
      background: #218838;
    }
  `]
})
export class EsperandoComponent implements OnInit, OnDestroy {
  /** Numero unico de la compra, obtenido del parametro de ruta `:numeroCompra`. */
  numeroCompra = 0;

  /** Estado actual de la compra (ej. `CREADA`, `APROBADA`, `RECHAZADA`). */
  estado = 'CREADA';

  /** Intervalo en segundos entre cada consulta de estado (polling). */
  intervalo = 5;

  /** URL del SaludPay-SPA para el boton de pago cuando la compra es APROBADA. */
  saludPayUrl = environment.saludPayUrl;

  /** Referencia al temporizador de `setInterval`, usada para limpiar el polling en `ngOnDestroy`. */
  private timer?: any;

  /**
   * @param route - Ruta activa de Angular para extraer el parametro `numeroCompra`.
   * @param service - Servicio de eventos de UI para consultar el estado de la compra.
   */
  constructor(private route: ActivatedRoute, private service: EventosUIW) {}

  /**
   * Inicializa el componente extrayendo el numero de compra de la URL
   * e iniciando el ciclo de polling.
   *
   * Lee el parametro de ruta `numeroCompra`, ejecuta una consulta inmediata
   * al estado de la compra y configura un `setInterval` que repite la consulta
   * cada {@link intervalo} segundos.
   */
  ngOnInit(): void {
    this.numeroCompra = Number(this.route.snapshot.paramMap.get('numeroCompra'));
    this.poll();
    this.timer = setInterval(() => this.poll(), this.intervalo * 1000);
  }

  /**
   * Limpia el temporizador de polling al destruir el componente.
   *
   * Evita que el `setInterval` continue ejecutandose cuando el usuario
   * navega a otra pagina, previniendo fugas de memoria y peticiones innecesarias.
   */
  ngOnDestroy(): void {
    this.detenerPolling();
  }

  /**
   * Indica si el estado actual de la compra ya es un estado final
   * (ya no cambiara por si solo). Cuando es final, se puede detener el polling.
   */
  estadoFinal(): boolean {
    return this.estado === 'APROBADA'
        || this.estado === 'RECHAZADA'
        || this.estado === 'TERMINADA';
  }

  /**
   * Realiza una consulta al backend para obtener el estado actual de la compra.
   *
   * Llama a {@link EventosUIW.consultarEstado} con el {@link numeroCompra}
   * y actualiza la propiedad {@link estado} con el valor recibido. Si el
   * estado pasa a ser final (APROBADA, RECHAZADA, TERMINADA) detiene el polling
   * para evitar requests innecesarios. Los errores de red se ignoran silenciosamente
   * para que el polling continue en la siguiente iteracion.
   */
  poll(): void {
    this.service.consultarEstado(this.numeroCompra).subscribe({
      next: (c: any) => {
        this.estado = c.estado;
        if (this.estadoFinal()) {
          this.detenerPolling();
        }
      },
      error: () => {}
    });
  }

  /** Limpia el temporizador de polling si esta activo. */
  private detenerPolling(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = undefined;
    }
  }
}
