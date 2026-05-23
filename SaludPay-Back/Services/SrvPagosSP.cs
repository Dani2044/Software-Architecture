using SaludPay.Api.Data;
using SaludPay.Api.Messaging;
using SaludPay.Api.Models;

namespace SaludPay.Api.Services;

public interface ISrvPagosSP
{
    bool RegistrarPago(string cedula, long numeroCompra, decimal valorPagado);
}

public class SrvPagosSP : ISrvPagosSP
{
    private readonly SaludPayDbContext _db;
    private readonly IPagoPublisher _publisher;
    private readonly ILogger<SrvPagosSP> _log;

    public SrvPagosSP(SaludPayDbContext db, IPagoPublisher publisher, ILogger<SrvPagosSP> log)
    {
        _db = db; _publisher = publisher; _log = log;
    }

    public bool RegistrarPago(string cedula, long numeroCompra, decimal valorPagado)
    {
        var compra = _db.ComprasPendientes
            .FirstOrDefault(c => c.NumeroCompra == numeroCompra && c.Cedula == cedula);
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

        _db.Pagos.Add(new Pago
        {
            NumeroCompra = numeroCompra,
            Cedula = cedula,
            ValorPagado = valorPagado,
            FechaPago = DateTime.UtcNow
        });
        _db.SaveChanges();

        // Notificar a MS-Compra via ColaPagoConfirmado
        _publisher.Publicar(new PagoMessage(cedula, numeroCompra, valorPagado, DateTime.UtcNow));
        return true;
    }
}
