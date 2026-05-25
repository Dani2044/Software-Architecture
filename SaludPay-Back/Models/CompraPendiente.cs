using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace SaludPay.Api.Models;

public class CompraPendiente
{
    // NumeroCompra es la PK pero NO es autogenerada: viene de MS-Compra (Java).
    // Sin DatabaseGenerated.None, EF Core la trata como IDENTITY en SQL Server
    // y rechaza el INSERT con un valor explicito.
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.None)]
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
