package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones de la capa de servicio (compartida con los controladores MVC) a
 * respuestas JSON con códigos HTTP apropiados para los controladores /api/**.
 * Se ordena antes que {@link GlobalExceptionHandler} para que este último (orientado a vistas
 * Thymeleaf) no intercepte las excepciones lanzadas desde los @RestController.
 */
@RestControllerAdvice(assignableTypes = {
    ProductoRestController.class,
    ProveedorRestController.class,
    VentaRestController.class,
    AuthRestController.class
})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestExceptionHandler {

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(org.springframework.security.core.AuthenticationException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.UNAUTHORIZED, "Credenciales invalidas", request);
    }

    /**
     * {@code @PreAuthorize} la lanza dentro de la propia ejecucion del controlador (AOP),
     * por lo que la resuelve este advice y no el AccessDeniedHandler del SecurityFilterChain
     * (ese solo actua sobre excepciones que llegan al filtro, no a las del handler de MVC).
     * Debe declararse explicitamente: si no, cae en el handleRuntime(RuntimeException) y
     * se devuelve como 400 en vez de 403.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.FORBIDDEN, "No tiene el rol necesario para acceder a este recurso", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return construir(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                                   HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        HttpStatus status = mensaje.toLowerCase().contains("no encontrad")
            ? HttpStatus.NOT_FOUND
            : HttpStatus.BAD_REQUEST;
        return construir(status, mensaje, request);
    }

    private ResponseEntity<ApiErrorResponse> construir(HttpStatus status, String mensaje, HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
