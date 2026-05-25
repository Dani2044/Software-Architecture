using SaludPay.Api.Data;
using SaludPay.Api.Messaging;
using SaludPay.Api.Models;

namespace SaludPay.Api.Services;

public interface ISrvPagosSP
{
    /// <summary>Registra el pago de una compra pendiente y notifica a MS-Compra.</summary>
    bool RegistrarPago(string cedula, long numeroCompra, decimal valorPagado);

    /// <summary>Crea una nueva compra pendiente (recibida desde MS-Compra cuando la SNS aprueba).</summary>
    string CrearCompraPendiente(string cedula, long numeroCompra, decimal valor);

    /// <summary>Lista las compras pendientes (no pagadas) asociadas a una cedula.</summary>
    List<CompraPendiente> ListarPendientes(string cedula);
}

/// <summary>
/// Servicio de pagos de SaludPay.
///
/// <para>Coordina el ciclo de vida de las compras pendientes y los pagos,
/// usando <see cref="IRepoSP"/> para acceso a datos y
/// <see cref="IPagoPublisher"/> (implementado por <c>IntegraConfirmacion</c>)
/// para notificar a MS-Compra via RabbitMQ cuando un pago se completa.</para>
/// </summary>
public class SrvPagosSP : ISrvPagosSP
{
    private readonly IRepoSP _repo;
    private readonly IPagoPublisher _publisher;
    private readonly ILogger<SrvPagosSP> _log;

    public SrvPagosSP(IRepoSP repo, IPagoPublisher publisher, ILogger<SrvPagosSP> log)
    {
        _repo = repo;
        _publisher = publisher;
        _log = log;
    }

    public string CrearCompraPendiente(string cedula, long numeroCompra, decimal valor)
    {
        var existente = _repo.BuscarCompraPendientePorNumero(numeroCompra);
        if (existente != null) return "ya_existia";

        _repo.AgregarCompraPendiente(new CompraPendiente
        {
            NumeroCompra = numeroCompra,
            Cedula = cedula,
            Valor = valor,
            Estado = EstadoCompraPendiente.PENDIENTE
        });
        _repo.Guardar();
        return "ok";
    }

    public List<CompraPendiente> ListarPendientes(string cedula) =>
        _repo.ListarPendientesPorCedula(cedula);

    public bool RegistrarPago(string cedula, long numeroCompra, decimal valorPagado)
    {
        var compra = _repo.BuscarCompraPendienteCedulaYNumero(cedula, numeroCompra);
        if (compra == null)
        {
            _log.LogWarning("Compra {n} no existe para cedula {c}", numeroCompra, cedula);
            return false;
        }
        if (compra.Estado == EstadoCompraPendiente.PAGADA)
        {
            _log.LogInformation("Compra {n} ya estaba pagada (idempotente)", numeroCompra);
            return true;
        }

        compra.Estado = EstadoCompraPendiente.PAGADA;
        compra.FechaPago = DateTime.UtcNow;

        _repo.AgregarPago(new Pago
        {
            NumeroCompra = numeroCompra,
            Cedula = cedula,
            ValorPagado = valorPagado,
            FechaPago = DateTime.UtcNow
        });
        _repo.Guardar();

        // Notificar a MS-Compra via ColaPagoConfirmado
        _publisher.Publicar(new PagoMessage(cedula, numeroCompra, valorPagado, DateTime.UtcNow));
        return true;
    }
}
