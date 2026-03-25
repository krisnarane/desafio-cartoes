package br.com.desafio.cartoes.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteValidatorTest {

    // --- CPF Tests ---

    @Test
    void given_cpf11digitos_when_isValidCPFFormat_then_retornaTrue() {
        assertThat(ClienteValidator.isValidCPFFormat("12345678901")).isTrue();
    }

    @Test
    void given_cpfComMascara_when_isValidCPFFormat_then_retornaTrue() {
        assertThat(ClienteValidator.isValidCPFFormat("123.456.789-01")).isTrue();
    }

    @Test
    void given_cpf10digitos_when_isValidCPFFormat_then_retornaFalse() {
        assertThat(ClienteValidator.isValidCPFFormat("1234567890")).isFalse();
    }

    @Test
    void given_cpf12digitos_when_isValidCPFFormat_then_retornaFalse() {
        assertThat(ClienteValidator.isValidCPFFormat("123456789012")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void given_cpfNuloOuVazio_when_isValidCPFFormat_then_retornaFalse(String cpf) {
        assertThat(ClienteValidator.isValidCPFFormat(cpf)).isFalse();
    }

    @Test
    void given_cpfComLetras_when_isValidCPFFormat_then_retornaFalse() {
        assertThat(ClienteValidator.isValidCPFFormat("1234567890a")).isFalse();
    }

    // --- UF Tests ---

    @ParameterizedTest
    @ValueSource(strings = {"SP", "RJ", "MG", "AC", "TO", "DF", "RS", "BA"})
    void given_ufValida_when_isValidUF_then_retornaTrue(String uf) {
        assertThat(ClienteValidator.isValidUF(uf)).isTrue();
    }

    @Test
    void given_ufInvalida_when_isValidUF_then_retornaFalse() {
        assertThat(ClienteValidator.isValidUF("XX")).isFalse();
    }

    @Test
    void given_ufMinuscula_when_isValidUF_then_retornaFalse() {
        assertThat(ClienteValidator.isValidUF("sp")).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void given_ufNulaOuVazia_when_isValidUF_then_retornaFalse(String uf) {
        assertThat(ClienteValidator.isValidUF(uf)).isFalse();
    }

    @Test
    void given_ufTresLetras_when_isValidUF_then_retornaFalse() {
        assertThat(ClienteValidator.isValidUF("SPP")).isFalse();
    }
}
