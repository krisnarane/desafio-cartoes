package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.repository.CartaoRepository;
import br.com.desafio.cartoes.rule.ElegibilidadeRule;
import br.com.desafio.cartoes.support.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElegibilidadeServiceTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private ElegibilidadeRule regra1;

    @Mock
    private ElegibilidadeRule regra2;

    private ElegibilidadeService service;

    @BeforeEach
    void setUp() {
        service = new ElegibilidadeService(cartaoRepository, List.of(regra1, regra2));
    }

    @Test
    void given_clienteValido_when_processar_then_aplicaTodasRegras() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));
        List<CartaoOferta> cartoes = TestFactory.todosCartoes();

        when(cartaoRepository.findByAtivoTrue()).thenReturn(cartoes);
        when(regra1.aplicar(any(Cliente.class), anyList())).thenReturn(cartoes);
        when(regra2.aplicar(any(Cliente.class), anyList())).thenReturn(cartoes);

        List<CartaoOferta> resultado = service.processar(cliente);

        assertThat(resultado).hasSize(3);
        verify(regra1).aplicar(cliente, cartoes);
        verify(regra2).aplicar(cliente, cartoes);
    }

    @Test
    void given_nenhumCartaoAtivo_when_processar_then_retornaVazia() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));

        when(cartaoRepository.findByAtivoTrue()).thenReturn(Collections.emptyList());

        List<CartaoOferta> resultado = service.processar(cliente);

        assertThat(resultado).isEmpty();
        verify(regra1, never()).aplicar(any(), anyList());
        verify(regra2, never()).aplicar(any(), anyList());
    }

    @Test
    void given_primeiraRegraEliminaTodos_when_processar_then_naoAplicaSegundaRegra() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));
        List<CartaoOferta> cartoes = TestFactory.todosCartoes();

        when(cartaoRepository.findByAtivoTrue()).thenReturn(cartoes);
        when(regra1.aplicar(any(Cliente.class), anyList())).thenReturn(Collections.emptyList());

        List<CartaoOferta> resultado = service.processar(cliente);

        assertThat(resultado).isEmpty();
        verify(regra1).aplicar(cliente, cartoes);
        verify(regra2, never()).aplicar(any(), anyList());
    }

    @Test
    void given_regraFiltraAlguns_when_processar_then_passaFiltradosParaProximaRegra() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));
        List<CartaoOferta> todosCartoes = TestFactory.todosCartoes();
        List<CartaoOferta> apenasUm = List.of(TestFactory.criarCartaoSemAnuidade());

        when(cartaoRepository.findByAtivoTrue()).thenReturn(todosCartoes);
        when(regra1.aplicar(any(Cliente.class), anyList())).thenReturn(apenasUm);
        when(regra2.aplicar(any(Cliente.class), anyList())).thenReturn(apenasUm);

        List<CartaoOferta> resultado = service.processar(cliente);

        assertThat(resultado).hasSize(1);
        verify(regra2).aplicar(cliente, apenasUm);
    }

    @Test
    void given_repositorioRetornaCartoes_when_processar_then_carregaApenasAtivos() {
        Cliente cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));
        List<CartaoOferta> cartoes = TestFactory.todosCartoes();

        when(cartaoRepository.findByAtivoTrue()).thenReturn(cartoes);
        when(regra1.aplicar(any(), anyList())).thenReturn(cartoes);
        when(regra2.aplicar(any(), anyList())).thenReturn(cartoes);

        service.processar(cliente);

        verify(cartaoRepository).findByAtivoTrue();
        verify(cartaoRepository, never()).findAll();
    }
}
