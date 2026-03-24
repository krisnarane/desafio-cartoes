package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.model.Cliente;

import java.util.List;

public interface ElegibilidadeRule {
    
    /**
     * Filtra os cartões elegíveis aplicando uma regra específica.
     * 
     * @param cliente cliente a ser analisado
     * @param cartoes lista de cartões disponíveis
     * @return cartões que passaram na regra (removeu os não elegíveis)
     */
    List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes);
}
