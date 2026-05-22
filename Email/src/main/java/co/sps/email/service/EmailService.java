package co.sps.email.service;

import co.sps.email.config.EmailProperties;
import co.sps.email.model.NotificacionEmail;
import co.sps.email.model.NotificacionEmailRepository;
import co.sps.email.model.NotificacionRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailProperties props;
    private final NotificacionEmailRepository repository;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine,
                        EmailProperties props, NotificacionEmailRepository repository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.props = props;
        this.repository = repository;
    }

    public NotificacionEmail enviar(NotificacionRequest request) {
        NotificacionEmail notificacion = crearRegistro(request);

        try {
            String html = construirHtml(request);
            enviarMime(request.getCorreoCliente(), notificacion.getAsunto(), html);
            notificacion.setEstado(NotificacionEmail.EstadoEnvio.ENVIADO);
            notificacion.setFechaEnvio(LocalDateTime.now());
            log.info("Correo enviado a {} — compra {}", request.getCorreoCliente(), request.getNumeroCompra());
        } catch (Exception ex) {
            notificacion.setEstado(NotificacionEmail.EstadoEnvio.FALLIDO);
            notificacion.setErrorMensaje(ex.getMessage());
            log.error("Error enviando correo a {} — compra {}: {}",
                    request.getCorreoCliente(), request.getNumeroCompra(), ex.getMessage());
        }

        return repository.save(notificacion);
    }

    public List<NotificacionEmail> consultarPorCompra(String numeroCompra) {
        return repository.findByNumeroCompra(numeroCompra);
    }

    public List<NotificacionEmail> consultarFallidos() {
        return repository.findByEstado(NotificacionEmail.EstadoEnvio.FALLIDO);
    }

    private NotificacionEmail crearRegistro(NotificacionRequest request) {
        NotificacionEmail n = new NotificacionEmail();
        n.setDestinatario(request.getCorreoCliente());
        n.setNumeroCompra(request.getNumeroCompra());
        n.setTipo(request.getTipo());
        n.setEstado(NotificacionEmail.EstadoEnvio.PENDIENTE);
        String asunto = switch (request.getTipo()) {
            case COMPRA_APROBADA_SNS -> "SPS — Su compra fue aprobada, puede continuar el pago";
            case COMPRA_COMPLETADA -> "SPS — Confirmacion de compra completada";
        };
        n.setAsunto(asunto);
        return n;
    }

    private String construirHtml(NotificacionRequest request) {
        Context ctx = new Context();
        ctx.setVariable("nombreCliente", request.getNombreCliente());
        ctx.setVariable("numeroCompra", request.getNumeroCompra());
        ctx.setVariable("valorCompra", request.getValorCompra());
        ctx.setVariable("urlPago", props.getSpsUrlPago());
        ctx.setVariable("saludPayUrl", props.getSaludPayUrl());

        String template = switch (request.getTipo()) {
            case COMPRA_APROBADA_SNS -> "compra-aprobada";
            case COMPRA_COMPLETADA -> "compra-completada";
        };
        return templateEngine.process(template, ctx);
    }

    private void enviarMime(String destinatario, String asunto, String html) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
        helper.setFrom(props.getFrom());
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(html, true);
        mailSender.send(mensaje);
    }
}
