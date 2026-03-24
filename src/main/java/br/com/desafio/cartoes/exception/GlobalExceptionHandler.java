package br.com.desafio.cartoes.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        log.warn("Erro de validação de entrada detectado");
        
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Bad Request");
        problem.setDetail("Validação de entrada falhou");
        
        // Agrupa erros por campo
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
        
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(problem);
    }
    
    /**
     * Trata erros de negócio (422 ou 400)
     * cliente menor de 18, renda inválida, etc.
     */
    @ExceptionHandler(ClienteInvalidoException.class)
    public ResponseEntity<ProblemDetail> handleClienteInvalidoException(
            ClienteInvalidoException ex) {
        
        log.warn("Regra de negócio violada: {} - {}", ex.getCodigoErro(), ex.getMessage());
        
        int statusCode = ex.getStatusCode();
        
        ProblemDetail problem = ProblemDetail.forStatus(statusCode);
        problem.setTitle(statusCode == 422 ? "Unprocessable Entity" : "Bad Request");
        problem.setDetail(ex.getMessage());
        problem.setProperty("codigo_erro", ex.getCodigoErro());
        problem.setProperty("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(statusCode).body(problem);
    }
    
    /**
     * Trata erros gerais não capturados (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(Exception ex) {
        
        log.error("Erro inesperado na aplicação", ex);
        
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("Um erro inesperado ocorreu. Nosso time foi notificado.");
        problem.setProperty("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
