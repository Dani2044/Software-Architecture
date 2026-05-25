using SaludPay.Api.Data;
using SaludPay.Api.Models;

namespace SaludPay.Api.Services;

public interface ISrvAuthSP
{
    Usuario? Login(string cedula, string password);
}

/// <summary>
/// Servicio de autenticacion de SaludPay.
///
/// <para>Verifica la identidad del usuario contra la base de datos a traves
/// del repositorio <see cref="IRepoSP"/>, sin conocer EF Core ni el DbContext.</para>
/// </summary>
public class SrvAuthSP : ISrvAuthSP
{
    private readonly IRepoSP _repo;

    public SrvAuthSP(IRepoSP repo) => _repo = repo;

    public Usuario? Login(string cedula, string password)
    {
        var u = _repo.BuscarUsuarioPorCedula(cedula);
        if (u == null) return null;
        return BCrypt.Net.BCrypt.Verify(password, u.PasswordHash) ? u : null;
    }
}
