using System.Text.Json;
using Apache.NMS;
using Apache.NMS.ActiveMQ;

namespace SaludPay.Api.Messaging;

public interface IPagoPublisher
{
    void Publicar(PagoMessage pago);
}

public record PagoMessage(string Cedula, long NumeroCompra, decimal ValorPagado, DateTime FechaPago);

/// <summary>
/// Publica mensajes a cola.pago en ActiveMQ.
/// Compatible con spring-boot-starter-activemq del lado MS-Compra:
/// el converter Jackson de Spring lee TextMessage JSON y deserializa al tipo destino.
/// </summary>
public class ActiveMqPagoPublisher : IPagoPublisher
{
    private readonly string _brokerUri;
    private readonly string _user;
    private readonly string _pass;
    private readonly string _cola;
    private readonly ILogger<ActiveMqPagoPublisher> _log;

    public ActiveMqPagoPublisher(IConfiguration cfg, ILogger<ActiveMqPagoPublisher> log)
    {
        // Conversion del esquema Spring tcp://host:port -> NMS activemq:tcp://host:port
        var url = Environment.GetEnvironmentVariable("ACTIVEMQ_URL")
                  ?? cfg["ActiveMq:Url"]
                  ?? "tcp://10.43.100.111:61616";
        _brokerUri = $"activemq:{url}";
        _user = Environment.GetEnvironmentVariable("ACTIVEMQ_USER") ?? "admin";
        _pass = Environment.GetEnvironmentVariable("ACTIVEMQ_PASS") ?? "admin";
        _cola = cfg["ActiveMq:ColaPago"] ?? "cola.pago";
        _log = log;
    }

    public void Publicar(PagoMessage pago)
    {
        try
        {
            var factory = new ConnectionFactory(_brokerUri);
            using var connection = factory.CreateConnection(_user, _pass);
            connection.Start();
            using var session = connection.CreateSession();
            var dest = session.GetQueue(_cola);
            using var producer = session.CreateProducer(dest);
            producer.DeliveryMode = MsgDeliveryMode.Persistent;

            var msg = session.CreateTextMessage(JsonSerializer.Serialize(pago, new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase
            }));
            // Header que Spring Jackson usa para resolver el POJO destino
            msg.Properties.SetString("_type", "com.sps.compra.messaging.PagoEvento");
            producer.Send(msg);

            _log.LogInformation("Publicado en {cola} pago de compra {n}", _cola, pago.NumeroCompra);
        }
        catch (Exception ex)
        {
            _log.LogError(ex, "Error al publicar en ActiveMQ");
            throw;
        }
    }
}
