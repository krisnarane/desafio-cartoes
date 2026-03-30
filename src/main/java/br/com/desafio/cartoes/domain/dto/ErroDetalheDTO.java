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
public class ErroDetalheDTO {
    
    @JsonProperty("app")
    private String app;
    
    @JsonProperty("tipo_erro")
    private String tipoErro;
    
    @JsonProperty("mensagem_interna")
    private String mensagemInterna;
}
