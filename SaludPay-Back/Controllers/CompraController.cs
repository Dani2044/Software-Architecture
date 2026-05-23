using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Data;
using SaludPay.Api.Models;

namespace SaludPay.Api.Controllers;

[ApiController]
[Route("api")]
public class CompraController : ControllerBase
{
    private readonly SaludPayDbContext _db;

    public CompraController(SaludPayDbContext db) => _db = db;

    public record CompraPendienteRequest(string Cedula, long NumeroCompra, decimal Valor);

    /// <summary>
    /// Endpoint llamado por MS-Compra cuando la SNS aprueba.
    /// SPS le dice a SaludPay "este cliente tiene esta compra pendiente por pagar".
    /// </summary>
    [HttpPost("compras-pendientes")]
    public IActionResult Crear([FromBody] CompraPendienteRequest req)
    {
        var existente = _db.ComprasPendientes.FirstOrDefault(c => c.NumeroCompra == req.NumeroCompra);
        if (existente != null) return Ok(new { status = "ya_existia" });

        _db.ComprasPendientes.Add(new CompraPendiente
        {
            NumeroCompra = req.NumeroCompra,
            Cedula = req.Cedula,
            Valor = req.Valor,
            Estado = EstadoCompraPendiente.PENDIENTE
        });
        _db.SaveChanges();
        return Created($"/api/compras/{req.NumeroCompra}", new { status = "ok" });
    }

    /// <summary>
    /// Listar compras pendientes para la cedula que se identifico en SaludPay-SPA.
    /// </summary>
    [HttpGet("compras/{cedula}")]
    public IActionResult Listar(string cedula)
    {
        var list = _db.ComprasPendientes
            .Where(c => c.Cedula == cedula && c.Estado == EstadoCompraPendiente.PENDIENTE)
            .OrderByDescending(c => c.FechaCreacion)
            .ToList();
        return Ok(list);
    }
}
