package co.sps.balanceador.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.sps.balanceador.service.HealthCheck;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TimerHC {

    private static final Logger log = LoggerFactory.getLogger(TimerHC.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final HealthCheck healthCheck;

    public TimerHC(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    @Scheduled(fixedDelayString = "#{balanceadorProperties.healthcheck.intervalMs}")
    public void tick() {
        log.debug("TimerHC tick @ {} -- disparando HealthCheck", LocalDateTime.now().format(FMT));
        healthCheck.checkAll();
    }
}
