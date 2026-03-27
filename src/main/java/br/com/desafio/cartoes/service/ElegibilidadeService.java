package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.repository.CartaoRepository;
import br.com.desafio.cartoes.rule.ElegibilidadeRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ElegibilidadeService {
    
    private final CartaoRepository cartaoRepository;
    private final List<ElegibilidadeRule> regras;
    
    /**
     * Processa elegibilidade do cliente aplicando as rules em sequência
     * 
     * @param cliente cliente a ser analisado
     * @return cartões elegíveis após todas as regras
     */
    public List<CartaoOferta> processar(Cliente cliente) {
        log.info("Processando elegibilidade do cliente: {}", cliente.getCpf());
        
        // Carrega cartões ativos
        List<CartaoOferta> cartoes = cartaoRepository.findByAtivoTrue();
        log.debug("Cartões carregados: {}", cartoes.size());

        if (cartoes.isEmpty()) {
            log.info("Nenhum cartão ativo encontrado para processar");
            return cartoes;
        }

        // Aplicar cada regra sucessivamente
        for (ElegibilidadeRule rule : regras) {
            int antes = cartoes.size();
            cartoes = rule.aplicar(cliente, cartoes);
            int depois = cartoes.size();
            
            log.debug("Regra {} aplicada: {} → {} cartões", 
                rule.getClass().getSimpleName(), antes, depois);
            
            // Otimização: se não há cartões, pode parar
            if (cartoes.isEmpty()) {
                log.debug("Nenhum cartão elegível após {}", rule.getClass().getSimpleName());
                break;
            }
        }
        
        log.info("Elegibilidade processada para {}: {} cartões aprovados", 
            cliente.getCpf(), cartoes.size());
        
        return cartoes;
    }
}
