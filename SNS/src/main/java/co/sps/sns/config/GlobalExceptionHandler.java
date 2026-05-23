package co.sps.sns.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el microservicio SNS.
 *
 * <p>Intercepta todas las excepciones no controladas en los controladores REST
 * y las transforma en respuestas HTTP estandarizadas con formato JSON. Esto
 * garantiza que MS-Compra (el cliente que consume este servicio via WebClient)
 * siempre reciba respuestas consistentes y parseables.</p>
 *
 * <p><b>Excepciones manejadas:</b></p>
 * <ul>
 *   <li>{@link IllegalArgumentException} - Errores de logica de negocio (HTTP 400)</li>
 *   <li>{@link MethodArgumentNotValidException} - Errores de validacion de campos (HTTP 400)</li>
 *   <li>{@link Exception} - Cualquier otro error inesperado (HTTP 500)</li>
 * </ul>
 *
 * @author SPS Team
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de argumento ilegal lanzadas por la logica de negocio.
     *
     * <p>Se activa cuando, por ejemplo, se intenta actualizar una solicitud
     * que no existe en la base de datos.</p>
     *
     * @param ex la excepcion capturada con el mensaje descriptivo del error
     * @return respuesta HTTP 400 (Bad Request) con el mensaje de error en formato JSON
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Solicitud invalida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maneja excepciones de validacion de campos anotados con Bean Validation
     * ({@code @NotBlank}, {@code @NotNull}, etc.).
     *
     * <p>Recopila todos los errores de campo y los concatena en un solo mensaje
     * separado por comas para facilitar la depuracion desde el cliente.</p>
     *
     * @param ex la excepcion de validacion que contiene los errores de campo
     * @return respuesta HTTP 400 (Bad Request) con los errores de validacion concatenados
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        // Recorre los errores de cada campo y los concatena en un solo string
        String errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", errores));
    }

    /**
     * Manejador de ultimo recurso para cualquier excepcion no prevista.
     *
     * <p>Registra el error completo (incluyendo stack trace) en los logs del servidor
     * y devuelve un mensaje generico al cliente para no exponer detalles internos.</p>
     *
     * @param ex la excepcion generica capturada
     * @return respuesta HTTP 500 (Internal Server Error) con mensaje generico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        log.error("Error interno SNS: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno del servicio SNS"));
    }
}
