package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.domain.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class RegraPorIdade implements ElegibilidadeRule {
    
    @Override
    public List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes) {
        // Se idade entre 18 e 25 anos (exclusivo), remove Cashback e Parceiros
        int idade = cliente.calcularIdade();
        if (idade >= 18 && idade < 25) {
            return cartoes.stream()
                .filter(c -> c.getTipoCartao() == TipoCartao.CARTAO_SEM_ANUIDADE)
                .toList();
        }
        // Se não (idade >= 25), permite todos
        return cartoes;
    }
}
