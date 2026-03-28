package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoResponseDTO;
import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.ResultadoElegibilidade;
import br.com.desafio.cartoes.exception.ClienteInvalidoException;
import br.com.desafio.cartoes.support.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    @Mock
    private ValidacaoClienteService validacaoService;

    @Mock
    private ElegibilidadeService elegibilidadeService;

    @InjectMocks
    private CartaoService cartaoService;

    private ClienteRequestDTO clienteDTO;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteDTO = TestFactory.criarClienteRequestDTO(30, "RJ", new BigDecimal("8000"));
        cliente = TestFactory.criarCliente(30, "RJ", new BigDecimal("8000"));
    }

    @Test
    void given_clienteValido_when_solicitar_then_retornaResponseComCartoes() {
        List<CartaoOferta> cartoes = TestFactory.todosCartoes();
        when(validacaoService.converterParaModelo(any())).thenReturn(cliente);
        when(elegibilidadeService.processar(any())).thenReturn(new ResultadoElegibilidade().comCartoes(cartoes));

        SolicitacaoResponseDTO resultado = cartaoService.solicitar(clienteDTO);

        assertThat(resultado.getCartoesOfertados()).hasSize(3);
        assertThat(resultado.getCliente()).isEqualTo(clienteDTO);
        verify(validacaoService).validar(clienteDTO);
    }

    @Test
    void given_clienteValido_when_solicitar_then_responseTemUUIDEData() {
        when(validacaoService.converterParaModelo(any())).thenReturn(cliente);
        when(elegibilidadeService.processar(any())).thenReturn(new ResultadoElegibilidade().comCartoes(TestFactory.todosCartoes()));

        SolicitacaoResponseDTO resultado = cartaoService.solicitar(clienteDTO);

        assertThat(resultado.getNumeroSolicitacao()).isNotNull().isNotEmpty();
        assertThat(resultado.getDataSolicitacao()).isNotNull();
    }

    @Test
    void given_clienteSemCartoesElegiveis_when_solicitar_then_retornaListaVazia() {
        when(validacaoService.converterParaModelo(any())).thenReturn(cliente);
        when(elegibilidadeService.processar(any())).thenReturn(new ResultadoElegibilidade().comRejeicao("Sem cartões elegíveis"));

        SolicitacaoResponseDTO resultado = cartaoService.solicitar(clienteDTO);

        assertThat(resultado.getCartoesOfertados()).isEmpty();
    }

    @Test
    void given_clienteInvalido_when_solicitar_then_lancaExcecao() {
        doThrow(new ClienteInvalidoException("CPF inválido", "CPF_INVALIDO", 400))
                .when(validacaoService).validar(any());

        assertThatThrownBy(() -> cartaoService.solicitar(clienteDTO))
                .isInstanceOf(ClienteInvalidoException.class)
                .hasMessage("CPF inválido");

        verify(elegibilidadeService, never()).processar(any());
    }

    @Test
    void given_clienteValido_when_solicitar_then_cartaoResponseTemStatusAprovado() {
        List<CartaoOferta> cartoes = List.of(TestFactory.criarCartaoSemAnuidade());
        when(validacaoService.converterParaModelo(any())).thenReturn(cliente);
        when(elegibilidadeService.processar(any())).thenReturn(new ResultadoElegibilidade().comCartoes(cartoes));

        SolicitacaoResponseDTO resultado = cartaoService.solicitar(clienteDTO);

        assertThat(resultado.getCartoesOfertados().get(0).getStatus()).isEqualTo("APROVADO");
        assertThat(resultado.getCartoesOfertados().get(0).getTipoCartao()).isEqualTo("CARTAO_SEM_ANUIDADE");
        assertThat(resultado.getCartoesOfertados().get(0).getValorLimiteDisponivel())
                .isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(resultado.getCartoesOfertados().get(0).getValorAnuidadeMensal())
                .isEqualByComparingTo(new BigDecimal("0.00"));
    }

    @Test
    void given_clienteValido_when_solicitar_then_chamaServicosNaOrdemCorreta() {
        when(validacaoService.converterParaModelo(any())).thenReturn(cliente);
        when(elegibilidadeService.processar(any())).thenReturn(new ResultadoElegibilidade().comRejeicao("Sem cartões elegíveis"));

        cartaoService.solicitar(clienteDTO);

        var inOrder = inOrder(validacaoService, elegibilidadeService);
        inOrder.verify(validacaoService).validar(clienteDTO);
        inOrder.verify(validacaoService).converterParaModelo(clienteDTO);
        inOrder.verify(elegibilidadeService).processar(cliente);
    }
}
