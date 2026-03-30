package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ErroResponseDTO {
    
    @JsonProperty("codigo")
    private String codigo;
    
    @JsonProperty("mensagem")
    private String mensagem;
    
    @JsonProperty("detalhe_erro")
    private ErroDetalheDTO detalheErro;
}
