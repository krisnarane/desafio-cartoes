package br.com.desafio.cartoes.exception;

import br.com.desafio.cartoes.domain.dto.ErroDetalheDTO;
import br.com.desafio.cartoes.domain.dto.ErroResponseDTO;
import br.com.desafio.cartoes.domain.enums.TipoErro;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String APP_NAME = "cartoes";
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        log.warn("Erro de validação de entrada detectado");
        
        // Agrupa erros por campo
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining("; "));
        
        ErroResponseDTO erro = ErroResponseDTO.builder()
            .codigo(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .mensagem("Validação de entrada falhou")
            .detalheErro(ErroDetalheDTO.builder()
                .app(APP_NAME)
                .tipoErro(TipoErro.VALIDACAO_ENTRADA.name())
                .mensagemInterna(errors)
                .build())
            .build();
        
        return ResponseEntity.badRequest().body(erro);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        log.warn("Corpo da requisição inválido ou ausente");

        ErroResponseDTO erro = ErroResponseDTO.builder()
            .codigo(String.valueOf(HttpStatus.BAD_REQUEST.value()))
            .mensagem("Corpo da requisição inválido ou ausente")
            .detalheErro(ErroDetalheDTO.builder()
                .app(APP_NAME)
                .tipoErro(TipoErro.VALIDACAO_ENTRADA.name())
                .mensagemInterna("Erro ao processar JSON: " + ex.getMessage())
                .build())
            .build();

        return ResponseEntity.badRequest().body(erro);
    }

    /**
     * Trata erros de negócio (422 ou 400)
     * cliente menor de 18, renda inválida, etc.
     */
    @ExceptionHandler(ClienteInvalidoException.class)
    public ResponseEntity<ErroResponseDTO> handleClienteInvalidoException(
            ClienteInvalidoException ex) {
        
        log.warn("Regra de negócio violada: {} - {}", ex.getCodigoErro(), ex.getMessage());
        
        int statusCode = ex.getStatusCode();
        
        ErroResponseDTO erro = ErroResponseDTO.builder()
            .codigo(String.valueOf(statusCode))
            .mensagem(ex.getMessage())
            .detalheErro(ErroDetalheDTO.builder()
                .app(APP_NAME)
                .tipoErro(TipoErro.REGRA_NEGOCIO.name())
                .mensagemInterna("Código de erro: " + ex.getCodigoErro())
                .build())
            .build();
        
        return ResponseEntity.status(statusCode).body(erro);
    }
    
    /**
     * Trata erros gerais não capturados (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> handleGeneralException(Exception ex) {
        
        log.error("Erro inesperado na aplicação", ex);
        
        ErroResponseDTO erro = ErroResponseDTO.builder()
            .codigo("500")
            .mensagem("Um erro inesperado ocorreu.")
            .detalheErro(ErroDetalheDTO.builder()
                .app(APP_NAME)
                .tipoErro(TipoErro.SERVICO_INDISPONIVEL.name())
                .mensagemInterna("Tivemos um problema, mas fique tranquilo que nosso time já foi avisado.")
                .build())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
