package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RegraPorRenda implements ElegibilidadeRule {
    
    @Override
    public List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes) {
        return cartoes.stream()
            .filter(c -> cliente.getRendaMensal().compareTo(c.getRendaMinima()) >= 0)
            .collect(Collectors.toList());
    }
}
