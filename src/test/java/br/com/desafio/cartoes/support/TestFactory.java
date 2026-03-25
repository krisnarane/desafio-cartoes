package br.com.desafio.cartoes.support;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.domain.model.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class TestFactory {

    private TestFactory() {}

    public static ClienteRequestDTO criarClienteRequestDTO(int idade, String uf, BigDecimal renda) {
        return ClienteRequestDTO.builder()
                .nome("João Silva")
                .cpf("12345678901")
                .idade(idade)
                .dataNascimento(LocalDate.now().minusYears(idade))
                .uf(uf)
                .rendaMensal(renda)
                .email("joao@email.com")
                .telefoneWhatsapp("11999999999")
                .build();
    }

    public static Cliente criarCliente(int idade, String uf, BigDecimal renda) {
        return Cliente.builder()
                .nome("João Silva")
                .cpf("12345678901")
                .idade(idade)
                .dataNascimento(LocalDate.now().minusYears(idade))
                .uf(uf)
                .rendaMensal(renda)
                .email("joao@email.com")
                .telefoneWhatsapp("11999999999")
                .build();
    }

    public static CartaoOferta criarCartaoSemAnuidade() {
        return CartaoOferta.builder()
                .id(1L)
                .tipoCartao(TipoCartao.CARTAO_SEM_ANUIDADE)
                .rendaMinima(new BigDecimal("3500.00"))
                .valorAnuidadeMensal(new BigDecimal("0.00"))
                .valorLimiteDisponivel(new BigDecimal("1000.00"))
                .ativo(true)
                .build();
    }

    public static CartaoOferta criarCartaoDeParceiros() {
        return CartaoOferta.builder()
                .id(2L)
                .tipoCartao(TipoCartao.CARTAO_DE_PARCEIROS)
                .rendaMinima(new BigDecimal("5500.00"))
                .valorAnuidadeMensal(new BigDecimal("10.00"))
                .valorLimiteDisponivel(new BigDecimal("3000.00"))
                .ativo(true)
                .build();
    }

    public static CartaoOferta criarCartaoComCashback() {
        return CartaoOferta.builder()
                .id(3L)
                .tipoCartao(TipoCartao.CARTAO_COM_CASHBACK)
                .rendaMinima(new BigDecimal("7500.00"))
                .valorAnuidadeMensal(new BigDecimal("20.00"))
                .valorLimiteDisponivel(new BigDecimal("5000.00"))
                .ativo(true)
                .build();
    }

    public static List<CartaoOferta> todosCartoes() {
        return List.of(criarCartaoSemAnuidade(), criarCartaoDeParceiros(), criarCartaoComCashback());
    }
}
