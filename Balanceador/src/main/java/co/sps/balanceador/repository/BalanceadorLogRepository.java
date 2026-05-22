package co.sps.balanceador.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.sps.balanceador.model.BalanceadorLog;

import java.util.List;

@Repository
public interface BalanceadorLogRepository extends JpaRepository<BalanceadorLog, Long> {
    List<BalanceadorLog> findTop100ByOrderByTimestampDesc();
    List<BalanceadorLog> findByTipo(String tipo);
}
