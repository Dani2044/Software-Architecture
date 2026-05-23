using System.ComponentModel.DataAnnotations;

namespace SaludPay.Api.Models;

public class CompraPendiente
{
    [Key]
    public long NumeroCompra { get; set; }

    [Required]
    [MaxLength(32)]
    public string Cedula { get; set; } = string.Empty;

    public decimal Valor { get; set; }

    public EstadoCompraPendiente Estado { get; set; } = EstadoCompraPendiente.PENDIENTE;

    public DateTime FechaCreacion { get; set; } = DateTime.UtcNow;
    public DateTime? FechaPago { get; set; }
}

public enum EstadoCompraPendiente
{
    PENDIENTE,
    PAGADA
}
