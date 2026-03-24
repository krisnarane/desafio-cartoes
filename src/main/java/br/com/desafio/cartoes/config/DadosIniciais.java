package br.com.desafio.cartoes.config;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.repository.CartaoRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@AllArgsConstructor
public class DadosIniciais {
    
    @Bean
    CommandLineRunner init(CartaoRepository cartaoRepository) {
        return args -> {
            // Limpa dados anteriores
            cartaoRepository.deleteAll();
            
            // Cartão 1: sem Anuidade
            CartaoOferta cartao1 = CartaoOferta.builder()
                .tipoCartao(TipoCartao.CARTAO_SEM_ANUIDADE)
                .rendaMinima(new BigDecimal("3500.00"))
                .valorAnuidadeMensal(new BigDecimal("0.00"))
                .valorLimiteDisponivel(new BigDecimal("1000.00"))
                .ativo(true)
                .build();
            
            // Cartão 2: com Cashback
            CartaoOferta cartao2 = CartaoOferta.builder()
                .tipoCartao(TipoCartao.CARTAO_COM_CASHBACK)
                .rendaMinima(new BigDecimal("7500.00"))
                .valorAnuidadeMensal(new BigDecimal("20.00"))
                .valorLimiteDisponivel(new BigDecimal("5000.00"))
                .ativo(true)
                .build();
            
            // Cartão 3: de Parceiros
            CartaoOferta cartao3 = CartaoOferta.builder()
                .tipoCartao(TipoCartao.CARTAO_DE_PARCEIROS)
                .rendaMinima(new BigDecimal("5500.00"))
                .valorAnuidadeMensal(new BigDecimal("10.00"))
                .valorLimiteDisponivel(new BigDecimal("3000.00"))
                .ativo(true)
                .build();
            
            cartaoRepository.saveAll(List.of(cartao1, cartao2, cartao3));
        };
    }
}
