package br.com.desafio.cartoes.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoResponseDTO {
    
    private String numeroSolicitacao;
    private LocalDateTime dataSolicitacao;
    private ClienteRequestDTO cliente;
    private List<CartaoResponseDTO> cartoesOfertados;
}
