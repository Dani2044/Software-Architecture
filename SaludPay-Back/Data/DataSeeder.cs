using SaludPay.Api.Models;

namespace SaludPay.Api.Data;

public static class DataSeeder
{
    public static void Seed(SaludPayDbContext db)
    {
        if (!db.Usuarios.Any())
        {
            db.Usuarios.Add(new Usuario
            {
                Cedula = "1000000001",
                Nombre = "Juan Rozo",
                PasswordHash = BCrypt.Net.BCrypt.HashPassword("juan123")
            });
            db.SaveChanges();
        }
    }
}
