package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErroResponseDTO {
    
    @JsonProperty("codigo")
    private String codigo;
    
    @JsonProperty("mensagem")
    private String mensagem;
    
    @JsonProperty("detalhe_erro")
    private ErroDetalheDTO detalheErro;
}
