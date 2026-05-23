package com.sps.authcatalogo.config;

import com.sps.authcatalogo.auth.Usuario;
import com.sps.authcatalogo.auth.RepoAuth;
import com.sps.authcatalogo.catalogo.PlanSalud;
import com.sps.authcatalogo.catalogo.RepoCatalogo;
import com.sps.authcatalogo.catalogo.ServicioMedico;
import com.sps.authcatalogo.catalogo.ServicioMedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Componente de carga inicial de datos (seed) que se ejecuta al arrancar la aplicacion.
 *
 * <p>Implementa {@link CommandLineRunner} para poblar la base de datos con datos
 * de prueba cuando las tablas estan vacias. Esto permite que el microservicio
 * funcione de inmediato en entornos de desarrollo sin necesidad de scripts SQL externos.</p>
 *
 * <p>Datos creados:</p>
 * <ul>
 *   <li><b>Usuario:</b> juan / juan123 (contrasena cifrada con BCrypt)</li>
 *   <li><b>Servicios medicos:</b> Consulta medicina general, Examen de sangre, Hospitalizacion 1 dia</li>
 *   <li><b>Planes:</b> Plan Basico (consulta + examen) y Plan Premium (consulta + examen + hospitalizacion)</li>
 * </ul>
 *
 * <p>La carga es idempotente: si ya existen registros, no se insertan datos duplicados.</p>
 *
 * @see com.sps.authcatalogo.auth.Usuario
 * @see com.sps.authcatalogo.catalogo.PlanSalud
 * @see com.sps.authcatalogo.catalogo.ServicioMedico
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RepoAuth repoAuth;
    private final RepoCatalogo repoCatalogo;
    private final ServicioMedicoRepository servicioRepository;
    private final PasswordEncoder encoder;

    /**
     * Metodo ejecutado automaticamente al iniciar la aplicacion.
     * Invoca la carga de usuarios y del catalogo en orden secuencial.
     *
     * @param args argumentos de linea de comandos (no utilizados)
     */
    @Override
    public void run(String... args) {
        seedUsuarios();
        seedCatalogo();
    }

    /**
     * Crea el usuario de prueba por defecto si la tabla de usuarios esta vacia.
     *
     * <p>Credenciales: {@code juan / juan123}. La contrasena se cifra con BCrypt
     * antes de persistirse, garantizando que nunca se almacene en texto plano.</p>
     */
    private void seedUsuarios() {
        // Evita duplicados: solo inserta si no hay usuarios en la tabla
        if (repoAuth.count() > 0) return;
        repoAuth.save(Usuario.builder()
                .username("juan")
                .passwordHash(encoder.encode("juan123"))
                .cedula("1000000001")
                .nombre("Juan Rozo")
                .correo("juan@sps.local")
                .build());
        log.info("Usuario seed creado: juan/juan123");
    }

    /**
     * Crea los servicios medicos y planes de salud de prueba si la tabla de servicios esta vacia.
     *
     * <p>Se crean primero los servicios medicos individuales y luego los planes
     * que los agrupan, respetando la dependencia de la relacion ManyToMany.</p>
     */
    private void seedCatalogo() {
        // Evita duplicados: solo inserta si no hay servicios en la tabla
        if (servicioRepository.count() > 0) return;

        // --- Servicios medicos individuales ---
        ServicioMedico consulta = servicioRepository.save(ServicioMedico.builder()
                .codigo("SVC-CONS-001")
                .nombre("Consulta medicina general")
                .tipo(ServicioMedico.TipoServicio.CONSULTA)
                .precio(new BigDecimal("50000"))
                .duracionMinutos(20)
                .build());

        ServicioMedico examen = servicioRepository.save(ServicioMedico.builder()
                .codigo("SVC-EXAM-001")
                .nombre("Examen de sangre")
                .tipo(ServicioMedico.TipoServicio.EXAMEN)
                .precio(new BigDecimal("80000"))
                .duracionMinutos(15)
                .build());

        ServicioMedico hospitalizacion = servicioRepository.save(ServicioMedico.builder()
                .codigo("SVC-HOSP-001")
                .nombre("Hospitalizacion 1 dia")
                .tipo(ServicioMedico.TipoServicio.HOSPITALIZACION)
                .precio(new BigDecimal("500000"))
                // 1440 minutos = 24 horas (1 dia completo)
                .duracionMinutos(1440)
                .build());

        // --- Planes de salud que agrupan servicios ---
        repoCatalogo.save(PlanSalud.builder()
                .codigo("PLAN-BASICO-001")
                .nombre("Plan Basico")
                .precio(new BigDecimal("130000"))
                .descripcion("Consulta + Examen")
                .servicios(List.of(consulta, examen))
                .build());

        repoCatalogo.save(PlanSalud.builder()
                .codigo("PLAN-PREMIUM-001")
                .nombre("Plan Premium")
                .precio(new BigDecimal("630000"))
                .descripcion("Consulta + Examen + Hospitalizacion")
                .servicios(List.of(consulta, examen, hospitalizacion))
                .build());

        log.info("Catalogo seed creado: 2 planes, 3 servicios medicos");
    }
}
