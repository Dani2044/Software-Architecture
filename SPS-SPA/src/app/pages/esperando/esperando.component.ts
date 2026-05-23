import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { CompraService } from '../../services/compra.service';

/**
 * Componente de espera que muestra el estado de procesamiento de una compra.
 *
 * Tras confirmar una compra, el usuario es redirigido a esta pantalla donde
 * se muestra el numero de compra y su estado actual. El componente realiza
 * polling automatico cada {@link intervalo} segundos (por defecto 5) al
 * endpoint de consulta de estado del {@link CompraService}.
 *
 * La compra pasa por varios estados (ej. `CREADA`, `APROBADA`, `RECHAZADA`)
 * a medida que la SNS (Superintendencia Nacional de Salud) procesa la solicitud.
 * Cuando la compra es aprobada, el sistema envia un correo con el link de pago.
 *
 * El polling se detiene automaticamente al destruir el componente (`ngOnDestroy`),
 * limpiando el `setInterval` para evitar fugas de memoria.
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
      <small>Esta pagina se refresca automaticamente cada {{ intervalo }}s.</small>
    </div>
  `
})
export class EsperandoComponent implements OnInit, OnDestroy {
  /** Numero unico de la compra, obtenido del parametro de ruta `:numeroCompra`. */
  numeroCompra = 0;

  /** Estado actual de la compra (ej. `CREADA`, `APROBADA`, `RECHAZADA`). */
  estado = 'CREADA';

  /** Intervalo en segundos entre cada consulta de estado (polling). */
  intervalo = 5;

  /** Referencia al temporizador de `setInterval`, usada para limpiar el polling en `ngOnDestroy`. */
  private timer?: any;

  /**
   * @param route - Ruta activa de Angular para extraer el parametro `numeroCompra`.
   * @param service - Servicio de compras para consultar el estado de la compra.
   */
  constructor(private route: ActivatedRoute, private service: CompraService) {}

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
    if (this.timer) clearInterval(this.timer);
  }

  /**
   * Realiza una consulta al backend para obtener el estado actual de la compra.
   *
   * Llama a {@link CompraService.consultarEstado} con el {@link numeroCompra}
   * y actualiza la propiedad {@link estado} con el valor recibido. Los errores
   * de red se ignoran silenciosamente para que el polling continue en la
   * siguiente iteracion.
   */
  poll(): void {
    this.service.consultarEstado(this.numeroCompra).subscribe({
      next: (c: any) => this.estado = c.estado,
      error: () => {}
    });
  }
}
