package co.sps.email.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email")
public class EmailProperties {

    private String from;
    private String saludPayUrl;
    private String spsUrlPago;

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }
    public String getSaludPayUrl() { return saludPayUrl; }
    public void setSaludPayUrl(String saludPayUrl) { this.saludPayUrl = saludPayUrl; }
    public String getSpsUrlPago() { return spsUrlPago; }
    public void setSpsUrlPago(String spsUrlPago) { this.spsUrlPago = spsUrlPago; }
}
