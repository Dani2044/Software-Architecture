using Microsoft.EntityFrameworkCore;
using SaludPay.Api.Data;
using SaludPay.Api.Messaging;
using SaludPay.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// --- Configuracion ---
builder.WebHost.ConfigureKestrel(o => o.ListenAnyIP(5000));

var dbHost = Environment.GetEnvironmentVariable("DB_HOST") ?? "sqlserver-saludpay";
var dbPass = Environment.GetEnvironmentVariable("DB_PASS") ?? "SaludPay123!";
var connectionString =
    $"Server={dbHost},1433;Database=SaludPayDb;User Id=sa;Password={dbPass};TrustServerCertificate=True";

builder.Services.AddDbContext<SaludPayDbContext>(o => o.UseSqlServer(connectionString));

// CORS abierto para los SPA
builder.Services.AddCors(o => o.AddDefaultPolicy(p =>
    p.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader()));

builder.Services.AddSingleton<IPagoPublisher, IntegraConfirmacion>();
builder.Services.AddScoped<ISrvAuthSP, SrvAuthSP>();
builder.Services.AddScoped<ISrvPagosSP, SrvPagosSP>();

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// Migra/crea la BD en arranque (suficiente para el alcance del proyecto)
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<SaludPayDbContext>();
    db.Database.EnsureCreated();
    DataSeeder.Seed(db);
}

app.UseCors();
app.UseSwagger();
app.UseSwaggerUI();
app.MapControllers();
app.MapGet("/api/saludpay/health", () => Results.Ok(new { status = "UP", service = "SaludPay-Back" }));

app.Run();
