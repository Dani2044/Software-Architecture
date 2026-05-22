package co.sps.balanceador.service;

import co.sps.balanceador.model.BalanceadorLog;
import co.sps.balanceador.repository.BalanceadorLogRepository;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final BalanceadorLogRepository repo;

    public LogService(BalanceadorLogRepository repo) {
        this.repo = repo;
    }

    @Async
    public void registrar(String tipo, String metodo, String backendUrl, String path, String detalle) {
        try {
            repo.save(new BalanceadorLog(tipo, metodo, backendUrl, path, detalle));
        } catch (Exception e) { }
    }
}
