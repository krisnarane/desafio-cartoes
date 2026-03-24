package br.com.desafio.cartoes.domain.dto;

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
    
    private String tipoCartao;
    private BigDecimal valorAnuidadeMensal;
    private BigDecimal valorLimiteDisponivel;
    private String status;
}
