package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.exception.ClienteInvalidoException;
import br.com.desafio.cartoes.support.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ValidacaoClienteServiceTest {

    private ValidacaoClienteService service;

    @BeforeEach
    void setUp() {
        service = new ValidacaoClienteService();
    }

    // --- CPF Validation ---

    @Test
    void given_cpfInvalido_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("5000"));
        dto.setCpf("123");

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("CPF_INVALIDO");
        assertThat(ex.getStatusCode()).isEqualTo(400);
    }

    @Test
    void given_cpfVazio_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("5000"));
        dto.setCpf("");

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("CPF_INVALIDO");
    }

    @Test
    void given_cpfValido11digitos_when_validar_then_naoLancaExcecao() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("5000"));
        dto.setCpf("12345678901");

        assertThatNoException().isThrownBy(() -> service.validar(dto));
    }

    @Test
    void given_cpfComMascara_when_validar_then_naoLancaExcecao() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("5000"));
        dto.setCpf("123.456.789-01");

        assertThatNoException().isThrownBy(() -> service.validar(dto));
    }

    // --- UF Validation ---

    @Test
    void given_ufInvalida_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "XX", new BigDecimal("5000"));

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("UF_INVALIDA");
        assertThat(ex.getStatusCode()).isEqualTo(400);
    }

    @Test
    void given_ufMinuscula_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "sp", new BigDecimal("5000"));

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("UF_INVALIDA");
    }

    // --- Age Validation ---

    @Test
    void given_menorDe18_when_validar_then_lancaExcecao422() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(17, "SP", new BigDecimal("5000"));
        dto.setDataNascimento(LocalDate.now().minusYears(17));

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("CLIENTE_MENOR_DE_IDADE");
        assertThat(ex.getStatusCode()).isEqualTo(422);
    }

    @Test
    void given_exatamente18Anos_when_validar_then_naoLancaExcecao() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(18, "SP", new BigDecimal("5000"));
        dto.setDataNascimento(LocalDate.now().minusYears(18));

        assertThatNoException().isThrownBy(() -> service.validar(dto));
    }

    // --- Income Validation ---

    @Test
    void given_rendaZero_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", BigDecimal.ZERO);

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("RENDA_INVALIDA");
        assertThat(ex.getStatusCode()).isEqualTo(400);
    }

    @Test
    void given_rendaNegativa_when_validar_then_lancaExcecao400() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("-1000"));

        ClienteInvalidoException ex = catchThrowableOfType(
                ClienteInvalidoException.class, () -> service.validar(dto));

        assertThat(ex.getCodigoErro()).isEqualTo("RENDA_INVALIDA");
        assertThat(ex.getStatusCode()).isEqualTo(400);
    }

    // --- Valid Client ---

    @Test
    void given_clienteValido_when_validar_then_naoLancaExcecao() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(30, "RJ", new BigDecimal("5000"));

        assertThatNoException().isThrownBy(() -> service.validar(dto));
    }

    // --- Conversion ---

    @Test
    void given_clienteRequestDTO_when_converterParaModelo_then_camposCorretos() {
        ClienteRequestDTO dto = TestFactory.criarClienteRequestDTO(25, "SP", new BigDecimal("5000"));

        Cliente cliente = service.converterParaModelo(dto);

        assertThat(cliente.getNome()).isEqualTo(dto.getNome());
        assertThat(cliente.getCpf()).isEqualTo(dto.getCpf());
        assertThat(cliente.getDataNascimento()).isEqualTo(dto.getDataNascimento());
        assertThat(cliente.getUf()).isEqualTo(dto.getUf());
        assertThat(cliente.getRendaMensal()).isEqualByComparingTo(dto.getRendaMensal());
        assertThat(cliente.getEmail()).isEqualTo(dto.getEmail());
        assertThat(cliente.getTelefoneWhatsapp()).isEqualTo(dto.getTelefoneWhatsapp());
    }
}
