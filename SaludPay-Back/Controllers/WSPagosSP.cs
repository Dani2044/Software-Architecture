using Microsoft.AspNetCore.Mvc;
using SaludPay.Api.Services;

namespace SaludPay.Api.Controllers;

/// <summary>
/// Controller HTTP unico (Web Service) de SaludPay.
///
/// <para>Alineado con el diagrama de despliegue UML del nodo SaludPay (VM-4 DMZ),
/// donde <c>WSPagosSP</c> es el unico punto de entrada externo del componente
/// <c>SaludPay.dll</c>. Expone 4 endpoints agrupados por funcionalidad:</para>
///
/// <list type="bullet">
///   <item><b>Auth:</b>    <c>POST /api/saludpay/auth/login</c> — login del usuario en SaludPay.</item>
///   <item><b>Compras:</b> <c>POST /api/compras-pendientes</c>  — alta de una compra pendiente (llamado por MS-Compra).</item>
///   <item><b>Compras:</b> <c>GET  /api/compras/{cedula}</c>    — lista de pendientes de un usuario (llamado por SaludPay-SPA).</item>
///   <item><b>Pago:</b>    <c>POST /api/pago</c>                — registrar el pago y disparar <c>IntegraConfirmacion</c>.</item>
/// </list>
///
/// <para>Delega la logica a <see cref="ISrvAuthSP"/> y <see cref="ISrvPagosSP"/>;
/// no toca el DbContext directamente.</para>
/// </summary>
[ApiController]
[Route("api")]
public class WSPagosSP : ControllerBase
{
    private readonly ISrvAuthSP _auth;
    private readonly ISrvPagosSP _pagos;

    public WSPagosSP(ISrvAuthSP auth, ISrvPagosSP pagos)
    {
        _auth = auth;
        _pagos = pagos;
    }

    // ─── DTOs ───
    public record LoginRequest(string Cedula, string Password);
    public record PagoRequest(string Cedula, long NumeroCompra, decimal ValorPagado);
    public record CompraPendienteRequest(string Cedula, long NumeroCompra, decimal Valor);

    // ─── Auth ───
    [HttpPost("saludpay/auth/login")]
    public IActionResult Login([FromBody] LoginRequest req)
    {
        var u = _auth.Login(req.Cedula, req.Password);
        if (u == null) return Unauthorized(new { error = "Credenciales invalidas" });
        return Ok(new { cedula = u.Cedula, nombre = u.Nombre });
    }

    // ─── Compras pendientes ───
    /// <summary>
    /// Llamado por MS-Compra cuando la SNS aprueba: SPS informa a SaludPay que
    /// existe una compra pendiente por pagar para una cedula.
    /// </summary>
    [HttpPost("compras-pendientes")]
    public IActionResult CrearCompraPendiente([FromBody] CompraPendienteRequest req)
    {
        var resultado = _pagos.CrearCompraPendiente(req.Cedula, req.NumeroCompra, req.Valor);
        if (resultado == "ya_existia") return Ok(new { status = "ya_existia" });
        return Created($"/api/compras/{req.NumeroCompra}", new { status = "ok" });
    }

    /// <summary>
    /// Llamado por SaludPay-SPA tras el login para mostrar las compras pendientes
    /// asociadas a la cedula del usuario autenticado.
    /// </summary>
    [HttpGet("compras/{cedula}")]
    public IActionResult ListarPendientes(string cedula)
    {
        var lista = _pagos.ListarPendientes(cedula);
        return Ok(lista);
    }

    // ─── Pago ───
    [HttpPost("pago")]
    public IActionResult Pagar([FromBody] PagoRequest req)
    {
        var ok = _pagos.RegistrarPago(req.Cedula, req.NumeroCompra, req.ValorPagado);
        if (!ok) return NotFound(new { error = "Compra no encontrada para esa cedula" });
        return Ok(new { status = "pagado", numeroCompra = req.NumeroCompra });
    }
}
