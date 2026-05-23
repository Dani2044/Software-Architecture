using Microsoft.AspNetCore.Mvc;
using SaludPay.Api.Services;

namespace SaludPay.Api.Controllers;

[ApiController]
[Route("api/pago")]
public class PagoController : ControllerBase
{
    private readonly IPagoService _pagoService;

    public PagoController(IPagoService pagoService) => _pagoService = pagoService;

    public record PagoRequest(string Cedula, long NumeroCompra, decimal ValorPagado);

    [HttpPost]
    public IActionResult Pagar([FromBody] PagoRequest req)
    {
        var ok = _pagoService.RegistrarPago(req.Cedula, req.NumeroCompra, req.ValorPagado);
        if (!ok) return NotFound(new { error = "Compra no encontrada para esa cedula" });
        return Ok(new { status = "pagado", numeroCompra = req.NumeroCompra });
    }
}
