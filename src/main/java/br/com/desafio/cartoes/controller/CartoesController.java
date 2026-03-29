package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoResponseDTO;
import br.com.desafio.cartoes.service.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cartoes")
@AllArgsConstructor
@Slf4j
@Tag(name = "Cartões", description = "API para consultar cartões de crédito elegíveis")
public class CartoesController {
    
    private final CartaoService cartaoService;
    
    @PostMapping
    @Operation(summary = "Consultar cartões elegíveis para cliente",
        description = "Processa uma solicitação de cartões e retorna os elegíveis conforme regras de negócio")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cartões encontrados",
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = SolicitacaoResponseDTO.class))),
        @ApiResponse(responseCode = "204", description = "Nenhum cartão elegível (sem corpo)"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida (JSON malformado ou validação falhou)",
            content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: menor de 18 anos, renda inválida)",
            content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
            content = @Content(mediaType = "application/problem+json"))
    })
    public ResponseEntity<?> solicitar(
            @Valid @RequestBody SolicitacaoRequestDTO request) {

        ClienteRequestDTO cliente = request.getCliente();
        log.info("POST /cartoes recebido para CPF: {}", cliente.getCpf());

        // CartaoService valida e processa elegibilidade
        // Se houver erro de negócio, lança ClienteInvalidoException
        // Se JSON inválido, MethodArgumentNotValidException é lançado automaticamente
        SolicitacaoResponseDTO resultado = cartaoService.solicitar(cliente);

        // Se nenhum cartão elegível, retorna 204 No Content
        if (resultado.getCartoesOfertados().isEmpty()) {
            log.info("Nenhum cartão elegível para CPF: {} - retornando 204", cliente.getCpf());
            return ResponseEntity.noContent().build();
        }

        // Se tem cartões, retorna 200 OK com resultado
        log.info("Cartões encontrados para CPF {}: {} cartões - retornando 200",
            cliente.getCpf(), resultado.getCartoesOfertados().size());
        return ResponseEntity.status(HttpStatus.OK).body(resultado);
    }
}
