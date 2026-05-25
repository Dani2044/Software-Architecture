using SaludPay.Api.Models;

namespace SaludPay.Api.Data;

/// <summary>
/// Repositorio unificado de SaludPay (capa de acceso a datos).
///
/// <para>Es la UNICA clase del sistema que conoce <see cref="SaludPayDbContext"/>
/// y EF Core. Los services (<c>SrvAuthSP</c>, <c>SrvPagosSP</c>) dependen de
/// esta interfaz, no del DbContext, alineando el codigo con el diagrama de
/// despliegue UML del nodo SaludPay (VM-4 DMZ):</para>
///
/// <code>WSPagosSP -> SrvPagosSP/SrvAuthSP -> RepoSP -> SaludPayDbContext -> SQL Server</code>
///
/// <para>Si en el futuro se cambia el ORM o el motor de base de datos, solo
/// esta clase necesita modificarse.</para>
/// </summary>
public interface IRepoSP
{
    // ─── Usuarios ───
    Usuario? BuscarUsuarioPorCedula(string cedula);

    // ─── Compras pendientes ───
    CompraPendiente? BuscarCompraPendientePorNumero(long numeroCompra);
    CompraPendiente? BuscarCompraPendienteCedulaYNumero(string cedula, long numeroCompra);
    List<CompraPendiente> ListarPendientesPorCedula(string cedula);
    void AgregarCompraPendiente(CompraPendiente compra);

    // ─── Pagos ───
    void AgregarPago(Pago pago);

    // ─── Persistencia ───
    void Guardar();
}

/// <summary>
/// Implementacion concreta del repositorio basada en Entity Framework Core.
/// </summary>
public class RepoSP : IRepoSP
{
    private readonly SaludPayDbContext _db;

    public RepoSP(SaludPayDbContext db) => _db = db;

    public Usuario? BuscarUsuarioPorCedula(string cedula) =>
        _db.Usuarios.FirstOrDefault(u => u.Cedula == cedula);

    public CompraPendiente? BuscarCompraPendientePorNumero(long numeroCompra) =>
        _db.ComprasPendientes.FirstOrDefault(c => c.NumeroCompra == numeroCompra);

    public CompraPendiente? BuscarCompraPendienteCedulaYNumero(string cedula, long numeroCompra) =>
        _db.ComprasPendientes.FirstOrDefault(c => c.NumeroCompra == numeroCompra && c.Cedula == cedula);

    public List<CompraPendiente> ListarPendientesPorCedula(string cedula) =>
        _db.ComprasPendientes
            .Where(c => c.Cedula == cedula && c.Estado == EstadoCompraPendiente.PENDIENTE)
            .OrderByDescending(c => c.FechaCreacion)
            .ToList();

    public void AgregarCompraPendiente(CompraPendiente compra) => _db.ComprasPendientes.Add(compra);

    public void AgregarPago(Pago pago) => _db.Pagos.Add(pago);

    public void Guardar() => _db.SaveChanges();
}
