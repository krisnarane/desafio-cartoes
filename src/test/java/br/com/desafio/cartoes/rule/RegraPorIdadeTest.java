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

class RegraPorIdadeTest {

    private RegraPorIdade regra;
    private List<CartaoOferta> todosCartoes;

    @BeforeEach
    void setUp() {
        regra = new RegraPorIdade();
        todosCartoes = TestFactory.todosCartoes();
    }

    @Test
    void given_clienteIdade18_when_aplicar_then_retornaApenasSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(18, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCartao()).isEqualTo(TipoCartao.CARTAO_SEM_ANUIDADE);
    }

    @Test
    void given_clienteIdade24_when_aplicar_then_retornaApenasSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(24, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCartao()).isEqualTo(TipoCartao.CARTAO_SEM_ANUIDADE);
    }

    @Test
    void given_clienteIdade25_when_aplicar_then_retornaTodosCartoes() {
        Cliente cliente = TestFactory.criarCliente(25, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_clienteIdade30_when_aplicar_then_retornaTodosCartoes() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_clienteIdade50_when_aplicar_then_retornaTodosCartoes() {
        Cliente cliente = TestFactory.criarCliente(50, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado).hasSize(3);
    }

    @Test
    void given_listaVazia_when_aplicar_then_retornaVazia() {
        Cliente cliente = TestFactory.criarCliente(20, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, Collections.emptyList());

        assertThat(resultado).isEmpty();
    }

    @Test
    void given_clienteIdade18_when_aplicar_then_removeCashbackEParceiros() {
        Cliente cliente = TestFactory.criarCliente(18, "RJ", new BigDecimal("8000"));

        List<CartaoOferta> resultado = regra.aplicar(cliente, todosCartoes);

        assertThat(resultado)
                .extracting(CartaoOferta::getTipoCartao)
                .doesNotContain(TipoCartao.CARTAO_COM_CASHBACK, TipoCartao.CARTAO_DE_PARCEIROS);
    }

    @Test
    void given_apenasSemAnuidadeNaLista_clienteIdade20_when_aplicar_then_retornaSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(20, "RJ", new BigDecimal("8000"));
        List<CartaoOferta> lista = List.of(TestFactory.criarCartaoSemAnuidade());

        List<CartaoOferta> resultado = regra.aplicar(cliente, lista);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getTipoCartao()).isEqualTo(TipoCartao.CARTAO_SEM_ANUIDADE);
    }
}
