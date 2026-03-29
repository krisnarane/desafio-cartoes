package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaoResponseDTO {
    
    @JsonProperty("tipo_cartao")
    private String tipoCartao;
    
    @JsonProperty("valor_anuidade_mensal")
    private BigDecimal valorAnuidadeMensal;
    
    @JsonProperty("valor_limite_disponivel")
    private BigDecimal valorLimiteDisponivel;
    
    @JsonProperty("status")
    private String status;
}
