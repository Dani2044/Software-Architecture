package co.sps.balanceador.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "balanceador")
public class BalanceadorProperties {

    private List<String> backends;
    private String authCatalogoUrl;
    private Healthcheck healthcheck = new Healthcheck();

    public List<String> getBackends() { return backends; }
    public void setBackends(List<String> backends) { this.backends = backends; }

    public String getAuthCatalogoUrl() { return authCatalogoUrl; }
    public void setAuthCatalogoUrl(String authCatalogoUrl) { this.authCatalogoUrl = authCatalogoUrl; }

    public Healthcheck getHealthcheck() { return healthcheck; }
    public void setHealthcheck(Healthcheck healthcheck) { this.healthcheck = healthcheck; }

    public static class Healthcheck {
        private long intervalMs = 15000;
        public long getIntervalMs() { return intervalMs; }
        public void setIntervalMs(long intervalMs) { this.intervalMs = intervalMs; }
    }
}
