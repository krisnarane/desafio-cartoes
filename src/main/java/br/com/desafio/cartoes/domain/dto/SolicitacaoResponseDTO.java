package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("numero_solicitacao")
    private String numeroSolicitacao;
    
    @JsonProperty("data_solicitacao")
    private LocalDateTime dataSolicitacao;
    
    @JsonProperty("cliente")
    private ClienteRequestDTO cliente;
    
    @JsonProperty("cartoes_ofertados")
    private List<CartaoResponseDTO> cartoesOfertados;
}
