package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.support.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegraPorUFTest {

    private RegraPorUF regra;
    private List<CartaoOferta> todosCartoes;

    @BeforeEach
    void setUp() {
        regra = new RegraPorUF();
        todosCartoes = TestFactory.todosCartoes();
    }

    @Test
    void given_clienteSP_idade24_when_aplicar_then_removeParceiros() {
        Cliente cliente = TestFactory.criarCliente(24, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_COM_CASHBACK);
    }

    @Test
    void given_clienteSP_idade25_when_aplicar_then_retornaTodos() {
        Cliente cliente = TestFactory.criarCliente(25, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_clienteSP_idade29_when_aplicar_then_retornaTodos() {
        Cliente cliente = TestFactory.criarCliente(29, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_clienteSP_idade30_when_aplicar_then_removeParceiros() {
        Cliente cliente = TestFactory.criarCliente(30, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_COM_CASHBACK);
    }

    @Test
    void given_clienteSP_idade40_when_aplicar_then_removeParceiros() {
        Cliente cliente = TestFactory.criarCliente(40, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .doesNotContain(TipoCartao.CARTAO_DE_PARCEIROS);
    }

    @Test
    void given_clienteRJ_when_aplicar_then_retornaTodos() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_clienteMG_when_aplicar_then_retornaTodos() {
        Cliente cliente = TestFactory.criarCliente(20, "MG", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_listaVazia_when_aplicar_then_retornaVazia() {
        Cliente cliente = TestFactory.criarCliente(20, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, Collections.emptyList());

        assertThat(resultado).isEmpty();
    }

    @Test
    void given_clienteSP_idade18_when_aplicar_then_removeParceiros() {
        Cliente cliente = TestFactory.criarCliente(18, "SP", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_COM_CASHBACK);
    }
}
