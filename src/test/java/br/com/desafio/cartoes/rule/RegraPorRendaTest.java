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

class RegraPorRendaTest {

    private RegraPorRenda regra;
    private List<CartaoOferta> todosCartoes;

    @BeforeEach
    void setUp() {
        regra = new RegraPorRenda();
        todosCartoes = TestFactory.todosCartoes();
    }

    @Test
    void given_renda3500_when_aplicar_then_retornaApenasSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("3500"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCartao()).isEqualTo(TipoCartao.CARTAO_SEM_ANUIDADE);
    }

    @Test
    void given_renda5500_when_aplicar_then_retornaSemAnuidadeEParceiros() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("5500"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_DE_PARCEIROS);
    }

    @Test
    void given_renda7500_when_aplicar_then_retornaTodosCartoes() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("7500"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_renda3499_when_aplicar_then_retornaNenhum() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("3499"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).isEmpty();
    }

    @Test
    void given_renda5499_when_aplicar_then_retornaApenasSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("5499"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCartao()).isEqualTo(TipoCartao.CARTAO_SEM_ANUIDADE);
    }

    @Test
    void given_renda7499_when_aplicar_then_retornaSemAnuidadeEParceiros() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("7499"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .containsExactlyInAnyOrder(TipoCartao.CARTAO_SEM_ANUIDADE, TipoCartao.CARTAO_DE_PARCEIROS);
    }

    @Test
    void given_rendaMuitoAlta_when_aplicar_then_retornaTodosCartoes() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("50000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_listaVazia_when_aplicar_then_retornaVazia() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("10000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, Collections.emptyList());

        assertThat(resultado).isEmpty();
    }

    @Test
    void given_rendaExataIgualMinima_when_aplicar_then_cartaoIncluido() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("3500.00"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .contains(TipoCartao.CARTAO_SEM_ANUIDADE);
    }
}
