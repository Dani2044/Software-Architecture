using System.ComponentModel.DataAnnotations;

namespace SaludPay.Api.Models;

public class Pago
{
    [Key]
    public long Id { get; set; }

    [Required]
    public long NumeroCompra { get; set; }

    [Required]
    [MaxLength(32)]
    public string Cedula { get; set; } = string.Empty;

    public decimal ValorPagado { get; set; }

    public DateTime FechaPago { get; set; } = DateTime.UtcNow;
}
