using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Models;

namespace SaludPay.Api.Data;

public class SaludPayDbContext : DbContext
{
    public SaludPayDbContext(DbContextOptions<SaludPayDbContext> options) : base(options) { }

    public DbSet<Usuario> Usuarios => Set<Usuario>();
    public DbSet<CompraPendiente> ComprasPendientes => Set<CompraPendiente>();
    public DbSet<Pago> Pagos => Set<Pago>();

    protected override void OnModelCreating(ModelBuilder mb)
    {
        mb.Entity<CompraPendiente>()
          .Property(c => c.Valor)
          .HasPrecision(14, 2);

        mb.Entity<Pago>()
          .Property(p => p.ValorPagado)
          .HasPrecision(14, 2);
    }
}
