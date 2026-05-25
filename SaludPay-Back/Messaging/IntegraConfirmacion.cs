using System.Text;
using System.Text.Json;
using RabbitMQ.Client;

namespace SaludPay.Api.Messaging;

public interface IPagoPublisher
{
    void Publicar(PagoMessage pago);
}

public record PagoMessage(string Cedula, long NumeroCompra, decimal ValorPagado, DateTime FechaPago);

/// <summary>
/// Publica mensajes a ColaPagoConfirmado en RabbitMQ.
/// Compatible con spring-boot-starter-amqp del lado MS-Compra:
/// el Jackson2JsonMessageConverter de Spring AMQP lee el JSON del body
/// y usa el header __TypeId__ para resolver el POJO destino via DefaultClassMapper.
/// </summary>
public class IntegraConfirmacion : IPagoPublisher
{
    private readonly string _host;
    private readonly int _port;
    private readonly string _user;
    private readonly string _pass;
    private readonly string _cola;
    private readonly ILogger<IntegraConfirmacion> _log;

    public IntegraConfirmacion(IConfiguration cfg, ILogger<IntegraConfirmacion> log)
    {
        _host = Environment.GetEnvironmentVariable("RABBITMQ_HOST") ?? "10.43.100.122";
        _port = int.Parse(Environment.GetEnvironmentVariable("RABBITMQ_PORT") ?? "5672");
        _user = Environment.GetEnvironmentVariable("RABBITMQ_USER") ?? "admin";
        _pass = Environment.GetEnvironmentVariable("RABBITMQ_PASS") ?? "admin123";
        _cola = cfg["RabbitMq:ColaPago"] ?? "ColaPagoConfirmado";
        _log = log;
    }

    public void Publicar(PagoMessage pago)
    {
        try
        {
            var factory = new ConnectionFactory
            {
                HostName = _host,
                Port = _port,
                UserName = _user,
                Password = _pass
            };

            using var connection = factory.CreateConnection();
            using var channel = connection.CreateModel();

            // Declara la cola como durable (idempotente si ya existe)
            channel.QueueDeclare(
                queue: _cola,
                durable: true,
                exclusive: false,
                autoDelete: false,
                arguments: null
            );

            var props = channel.CreateBasicProperties();
            props.Persistent = true;
            props.ContentType = "application/json";
            props.ContentEncoding = "UTF-8";
            // Header __TypeId__ que Spring AMQP usa para resolver el POJO destino en MS-Compra
            props.Headers = new Dictionary<string, object>
            {
                { "__TypeId__", "TransaccionPago" }
            };

            var json = JsonSerializer.Serialize(pago, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            });
            var body = Encoding.UTF8.GetBytes(json);

            channel.BasicPublish(
                exchange: "",
                routingKey: _cola,
                basicProperties: props,
                body: body
            );

            _log.LogInformation("Publicado en {cola} pago de compra {n}", _cola, pago.NumeroCompra);
        }
        catch (Exception ex)
        {
            _log.LogError(ex, "Error al publicar en RabbitMQ");
            throw;
        }
    }
}
