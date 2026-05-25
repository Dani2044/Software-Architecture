package com.sps.shc.controller;

import com.sps.shc.entity.PlanSalud;
import com.sps.shc.entity.Usuario;
import com.sps.shc.repository.RepoSHC;
import com.sps.shc.repository.RepoUsuarioSHC;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para consultar registros de historia clinica.
 *
 * <p>Expone endpoints bajo el prefijo {@code /api/shc} que permiten:</p>
 * <ul>
 *   <li>Verificar el estado del servicio (health check).</li>
 *   <li>Consultar planes adquiridos por cedula del paciente.</li>
 *   <li>Consultar planes adquiridos por numero de compra.</li>
 *   <li>Consultar datos de un usuario (paciente) por cedula.</li>
 * </ul>
 *
 * <p>Estos endpoints son de solo lectura. La creacion de registros se realiza
 * exclusivamente a traves del listener JMS ({@link com.sps.shc.listener.ListenerSHC}).</p>
 *
 * @author SPS Team
 * @see com.sps.shc.repository.RepoSHC
 * @see com.sps.shc.repository.RepoUsuarioSHC
 */
@RestController
@RequestMapping("/api/shc")
@RequiredArgsConstructor
public class WSSHC {

    private final RepoSHC repoPlanes;
    private final RepoUsuarioSHC repoUsuarios;

    /**
     * Endpoint de verificacion de salud (health check) del microservicio.
     *
     * @return mapa con {@code status: "UP"} y {@code service: "SHC"}
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "SHC");
    }

    /**
     * Consulta todos los planes adquiridos asociados a una cedula de paciente.
     *
     * @param cedula numero de cedula del paciente
     * @return lista de planes adquiridos del paciente (puede estar vacia)
     */
    @GetMapping("/historia/{cedula}")
    public List<PlanSalud> porCedula(@PathVariable String cedula) {
        return repoPlanes.findByCedulaPaciente(cedula);
    }

    /**
     * Consulta todos los planes adquiridos generados a partir de un numero de compra.
     *
     * @param numeroCompra identificador de la compra de origen
     * @return lista de planes adquiridos en esa compra (puede estar vacia)
     */
    @GetMapping("/historia/compra/{numeroCompra}")
    public List<PlanSalud> porCompra(@PathVariable Long numeroCompra) {
        return repoPlanes.findByNumeroCompra(numeroCompra);
    }

    /**
     * Consulta los datos de un usuario (paciente) por su cedula.
     *
     * @param cedula numero de cedula del paciente
     * @return datos del usuario o 404 si no existe
     */
    @GetMapping("/usuario/{cedula}")
    public ResponseEntity<Usuario> usuario(@PathVariable String cedula) {
        return repoUsuarios.findByCedula(cedula)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
