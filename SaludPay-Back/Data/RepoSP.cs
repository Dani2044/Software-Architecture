using SaludPay.Api.Models;

namespace SaludPay.Api.Data;

/// <summary>
/// Repositorio unificado de SaludPay que expone operaciones de acceso a datos
/// a traves del DbContext. Abstrae el acceso directo al contexto EF Core.
/// </summary>
public class RepoSP
{
    private readonly SaludPayDbContext _db;

    public RepoSP(SaludPayDbContext db) => _db = db;

    public IQueryable<Usuario> Usuarios => _db.Usuarios;
    public IQueryable<CompraPendiente> ComprasPendientes => _db.ComprasPendientes;
    public IQueryable<Pago> Pagos => _db.Pagos;

    public void AgregarPago(Pago pago) => _db.Pagos.Add(pago);
    public void AgregarCompraPendiente(CompraPendiente compra) => _db.ComprasPendientes.Add(compra);
    public void Guardar() => _db.SaveChanges();
}
