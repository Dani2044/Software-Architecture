using Microsoft.AspNetCore.Mvc;
using SaludPay.Api.Services;

namespace SaludPay.Api.Controllers;

[ApiController]
[Route("api/saludpay/auth")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _auth;

    public AuthController(IAuthService auth) => _auth = auth;

    public record LoginRequest(string Cedula, string Password);

    [HttpPost("login")]
    public IActionResult Login([FromBody] LoginRequest req)
    {
        var u = _auth.Login(req.Cedula, req.Password);
        if (u == null) return Unauthorized(new { error = "Credenciales invalidas" });
        return Ok(new { cedula = u.Cedula, nombre = u.Nombre });
    }
}
