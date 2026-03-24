package br.com.desafio.cartoes.domain.model;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoElegibilidade {
    
    private List<CartaoOferta> cartoesAprovados;
    private String motivoRejeicao;
    private Boolean temRejeitados;
    
    public ResultadoElegibilidade comCartoes(List<CartaoOferta> cartoes) {
        this.cartoesAprovados = cartoes;
        this.temRejeitados = false;
        return this;
    }
    
    public ResultadoElegibilidade comRejeicao(String motivo) {
        this.motivoRejeicao = motivo;
        this.temRejeitados = true;
        this.cartoesAprovados = List.of();
        return this;
    }
}
