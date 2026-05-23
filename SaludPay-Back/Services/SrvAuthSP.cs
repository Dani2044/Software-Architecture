using SaludPay.Api.Data;
using SaludPay.Api.Models;

namespace SaludPay.Api.Services;

public interface ISrvAuthSP
{
    Usuario? Login(string cedula, string password);
}

public class SrvAuthSP : ISrvAuthSP
{
    private readonly SaludPayDbContext _db;

    public SrvAuthSP(SaludPayDbContext db) => _db = db;

    public Usuario? Login(string cedula, string password)
    {
        var u = _db.Usuarios.FirstOrDefault(x => x.Cedula == cedula);
        if (u == null) return null;
        return BCrypt.Net.BCrypt.Verify(password, u.PasswordHash) ? u : null;
    }
}
