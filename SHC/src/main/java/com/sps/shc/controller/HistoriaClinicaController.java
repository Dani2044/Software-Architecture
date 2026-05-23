package com.sps.shc.controller;

import com.sps.shc.entity.HistoriaClinica;
import com.sps.shc.repository.HistoriaClinicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para consultar registros de historia clínica.
 *
 * <p>Expone endpoints bajo el prefijo {@code /api/shc} que permiten:</p>
 * <ul>
 *   <li>Verificar el estado del servicio (health check).</li>
 *   <li>Consultar historias clínicas por cédula del paciente.</li>
 *   <li>Consultar historias clínicas por número de compra.</li>
 * </ul>
 *
 * <p>Estos endpoints son de solo lectura. La creación de registros se realiza
 * exclusivamente a través del listener JMS ({@link com.sps.shc.listener.ShcListener}).</p>
 *
 * @see com.sps.shc.repository.HistoriaClinicaRepository
 */
@RestController
@RequestMapping("/api/shc")
@RequiredArgsConstructor
public class HistoriaClinicaController {

    private final HistoriaClinicaRepository repository;

    /**
     * Endpoint de verificación de salud (health check) del microservicio.
     *
     * <p>Retorna un mapa JSON con el estado del servicio. Útil para monitoreo
     * y verificación por parte del balanceador de carga o herramientas de orquestación.</p>
     *
     * @return mapa con {@code status: "UP"} y {@code service: "SHC"}
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "SHC");
    }

    /**
     * Consulta todas las historias clínicas asociadas a una cédula de paciente.
     *
     * @param cedula número de cédula (documento de identidad) del paciente
     * @return lista de registros de historia clínica del paciente (puede estar vacía)
     */
    @GetMapping("/historia/{cedula}")
    public List<HistoriaClinica> porCedula(@PathVariable String cedula) {
        return repository.findByCedula(cedula);
    }

    /**
     * Consulta todas las historias clínicas generadas a partir de un número de compra.
     *
     * @param numeroCompra identificador de la compra de origen
     * @return lista de registros de historia clínica de la compra (puede estar vacía)
     */
    @GetMapping("/historia/compra/{numeroCompra}")
    public List<HistoriaClinica> porCompra(@PathVariable Long numeroCompra) {
        return repository.findByNumeroCompra(numeroCompra);
    }
}
