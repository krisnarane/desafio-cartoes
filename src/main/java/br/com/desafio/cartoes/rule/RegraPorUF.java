package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.domain.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegraPorUF implements ElegibilidadeRule {
    
    @Override
    public List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes) {
        // SP tem restrições especiais
        if ("SP".equals(cliente.getUf())) {
            int idade = cliente.calcularIdade();
            // Se entre 25 e 30 anos em SP, permite todos
            if (idade >= 25 && idade < 30) {
                return cartoes;
            }
            // Senão (< 25 ou >= 30) em SP, remove Parceiros
            return cartoes.stream()
                .filter(c -> c.getTipoCartao() != TipoCartao.CARTAO_DE_PARCEIROS)
                .collect(Collectors.toList());
        }
        // Fora de SP, permite todos
        return cartoes;
    }
}
