using System.ComponentModel.DataAnnotations;

namespace SaludPay.Api.Models;

public class Usuario
{
    [Key]
    [MaxLength(32)]
    public string Cedula { get; set; } = string.Empty;

    [MaxLength(200)]
    public string Nombre { get; set; } = string.Empty;

    [MaxLength(200)]
    public string PasswordHash { get; set; } = string.Empty;
}
