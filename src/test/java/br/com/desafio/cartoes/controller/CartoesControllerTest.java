package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.domain.dto.CartaoResponseDTO;
import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoResponseDTO;
import br.com.desafio.cartoes.exception.ClienteInvalidoException;
import br.com.desafio.cartoes.exception.GlobalExceptionHandler;
import br.com.desafio.cartoes.service.CartaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class) // integra para criar os mocks
class CartoesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartaoService cartaoService;

    @InjectMocks
    private CartoesController controller;

    private ObjectMapper objectMapper;

    // configurar mock com object mapper e snakecase
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    // json de modelo
    private String validRequestJson() {
        return """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": 30,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": 8000.00,
                        "email": "joao@email.com",
                        "telefone_whatsapp": "11999999999"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));
    }

    // retorna cliente com cartoes ofertados
    private SolicitacaoResponseDTO criarResponseComCartoes() {
        CartaoResponseDTO cartao = CartaoResponseDTO.builder()
                .tipoCartao("CARTAO_SEM_ANUIDADE")
                .valorAnuidadeMensal(new BigDecimal("0.00"))
                .valorLimiteDisponivel(new BigDecimal("1000.00"))
                .status("APROVADO")
                .build();

        return SolicitacaoResponseDTO.builder()
                .numeroSolicitacao(UUID.randomUUID().toString())
                .dataSolicitacao(LocalDateTime.now())
                .cliente(ClienteRequestDTO.builder()
                        .nome("João Silva")
                        .cpf("12345678901")
                        .dataNascimento(LocalDate.now().minusYears(30))
                        .uf("RJ")
                        .rendaMensal(new BigDecimal("8000"))
                        .email("joao@email.com")
                        .telefoneWhatsapp("11999999999")
                        .build())
                .cartoesOfertados(List.of(cartao))
                .build();
    }

    // retorna cliente sem cartoes
    private SolicitacaoResponseDTO criarResponseSemCartoes() {
        return SolicitacaoResponseDTO.builder()
                .numeroSolicitacao(UUID.randomUUID().toString())
                .dataSolicitacao(LocalDateTime.now())
                .cliente(ClienteRequestDTO.builder()
                        .nome("João Silva")
                        .cpf("12345678901")
                        .dataNascimento(LocalDate.now().minusYears(30))
                        .uf("RJ")
                        .rendaMensal(new BigDecimal("8000"))
                        .email("joao@email.com")
                        .telefoneWhatsapp("11999999999")
                        .build())
                .cartoesOfertados(Collections.emptyList())
                .build();
    }

    // simulando POST para /cartoes com JSON válido, retorna 200
    @Test
    void given_clienteValido_when_postCartoes_then_retorna200ComCartoes() throws Exception {
        when(cartaoService.solicitar(any())).thenReturn(criarResponseComCartoes());

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero_solicitacao").isNotEmpty())
                .andExpect(jsonPath("$.cartoes_ofertados").isArray())
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").value("CARTAO_SEM_ANUIDADE"))
                .andExpect(jsonPath("$.cartoes_ofertados[0].status").value("APROVADO"));
    }

    // cliente sem cartoes, retorna vazio 204
    @Test
    void given_clienteSemCartoes_when_postCartoes_then_retorna204() throws Exception {
        when(cartaoService.solicitar(any())).thenReturn(criarResponseSemCartoes());

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNoContent());
    }

    //testa campos faltando, retorna 400
    @Test
    void given_camposFaltando_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João"
                    }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // testando email invalido
    @Test
    void given_emailInvalido_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": 30,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": 8000.00,
                        "email": "invalido",
                        "telefone_whatsapp": "11999999999"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // testa renda negativa
    @Test
    void given_rendaNegativa_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": 30,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": -1000,
                        "email": "joao@email.com",
                        "telefone_whatsapp": "11999999999"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // testa menor de idade
    @Test
    void given_menorDeIdade_when_postCartoes_then_retorna422() throws Exception {
        when(cartaoService.solicitar(any()))
                .thenThrow(new ClienteInvalidoException(
                        "Cliente menor de 18 anos não é elegível",
                        "CLIENTE_MENOR_DE_IDADE", 422));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("422"))
                .andExpect(jsonPath("$.mensagem").value("Cliente menor de 18 anos não é elegível"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("REGRA_NEGOCIO"));
    }

    // testa formato de cpf invalido
    @Test
    void given_cpfInvalido_when_postCartoes_then_retorna400ViaExceptionHandler() throws Exception {
        when(cartaoService.solicitar(any()))
                .thenThrow(new ClienteInvalidoException("CPF formato inválido", "CPF_INVALIDO", 400));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.mensagem").value("CPF formato inválido"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("REGRA_NEGOCIO"));
    }

    // testa erro interno, 500
    @Test
    void given_erroInterno_when_postCartoes_then_retorna500() throws Exception {
        when(cartaoService.solicitar(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("500"))
                .andExpect(jsonPath("$.mensagem").value("Um erro inesperado ocorreu."))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("SERVICO_INDISPONIVEL"))
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna")
                        .value("Tivemos um problema, mas fique tranquilo que nosso time já foi avisado."));
    }

    // testa body vazio, retorna 400
    @Test
    void given_bodyVazio_when_postCartoes_then_retorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    // testa telefone invalido, 400
    @Test
    void given_telefoneInvalido_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": 30,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": 8000.00,
                        "email": "joao@email.com",
                        "telefone_whatsapp": "123"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // testa idade negativa, retorna 400
    @Test
    void given_idadeNegativa_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": -1,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": 8000.00,
                        "email": "joao@email.com",
                        "telefone_whatsapp": "11999999999"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    /**
     * Teste do contrato exato especificado pelo usuário
     * Entrada: JSON com CPF formatado (123.456.789-10)
     * Saída: Response com BigDecimal em 2 casas decimais
     */
    @Test
    void given_contratoExatoEspecificado_when_postCartoes_then_retornaComFormatoCorreto() throws Exception {
        // Payload de entrada exato do usuário
        String requestBody = """
                {
                    "cliente": {
                        "nome": "Cliente Teste",
                        "cpf": "123.456.789-10",
                        "idade": 25,
                        "data_nascimento": "2000-01-01",
                        "uf": "SP",
                        "renda_mensal": 4000,
                        "email": "cliente@teste.com",
                        "telefone_whatsapp": "11999992020"
                    }
                }
                """;

        // Response esperada com os dois cartões (sem anuidade e com cashback)
        CartaoResponseDTO cartaoSemAnuidade = CartaoResponseDTO.builder()
                .tipoCartao("CARTAO_SEM_ANUIDADE")
                .valorAnuidadeMensal(new BigDecimal("0.00"))
                .valorLimiteDisponivel(new BigDecimal("1000.00"))
                .status("APROVADO")
                .build();

        CartaoResponseDTO cartaoComCashback = CartaoResponseDTO.builder()
                .tipoCartao("CARTAO_COM_CASHBACK")
                .valorAnuidadeMensal(new BigDecimal("20.00"))
                .valorLimiteDisponivel(new BigDecimal("5000.00"))
                .status("APROVADO")
                .build();

        SolicitacaoResponseDTO responseEsperada = SolicitacaoResponseDTO.builder()
                .numeroSolicitacao("745f2812-c3f4-42ce-93fb-e119e643bda2")
                .dataSolicitacao(LocalDateTime.parse("2021-11-20T21:32:58.787"))
                .cliente(ClienteRequestDTO.builder()
                        .nome("Cliente Teste")
                        .cpf("123.456.789-10")
                        .idade(25)
                        .dataNascimento(LocalDate.parse("2000-01-01"))
                        .uf("SP")
                        .rendaMensal(new BigDecimal("4000.00"))
                        .email("cliente@teste.com")
                        .telefoneWhatsapp("11999992020")
                        .build())
                .cartoesOfertados(List.of(cartaoSemAnuidade, cartaoComCashback))
                .build();

        when(cartaoService.solicitar(any())).thenReturn(responseEsperada);

        // Executa requisição e valida resposta
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                // Valida estrutura base
                .andExpect(jsonPath("$.numero_solicitacao").exists())
                .andExpect(jsonPath("$.data_solicitacao").exists())
                .andExpect(jsonPath("$.cliente").exists())
                .andExpect(jsonPath("$.cartoes_ofertados").isArray())
                // Valida cliente com CPF formatado mantido
                .andExpect(jsonPath("$.cliente.nome").value("Cliente Teste"))
                .andExpect(jsonPath("$.cliente.cpf").value("123.456.789-10"))
                .andExpect(jsonPath("$.cliente.idade").value(25))
                .andExpect(jsonPath("$.cliente.data_nascimento").value("2000-01-01"))
                .andExpect(jsonPath("$.cliente.uf").value("SP"))
                // Valida renda_mensal com 2 casas decimais
                .andExpect(jsonPath("$.cliente.renda_mensal").value(4000.00))
                .andExpect(jsonPath("$.cliente.email").value("cliente@teste.com"))
                .andExpect(jsonPath("$.cliente.telefone_whatsapp").value("11999992020"))
                // Valida cartões
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").value("CARTAO_SEM_ANUIDADE"))
                .andExpect(jsonPath("$.cartoes_ofertados[0].valor_anuidade_mensal").value(0.00))
                .andExpect(jsonPath("$.cartoes_ofertados[0].valor_limite_disponivel").value(1000.00))
                .andExpect(jsonPath("$.cartoes_ofertados[0].status").value("APROVADO"))
                // Segundo cartão com cashback
                .andExpect(jsonPath("$.cartoes_ofertados[1].tipo_cartao").value("CARTAO_COM_CASHBACK"))
                .andExpect(jsonPath("$.cartoes_ofertados[1].valor_anuidade_mensal").value(20.00))
                .andExpect(jsonPath("$.cartoes_ofertados[1].valor_limite_disponivel").value(5000.00))
                .andExpect(jsonPath("$.cartoes_ofertados[1].status").value("APROVADO"));
    }

    /**
     * Teste para validar que CPF formatado é aceito na entrada
     */
    @Test
    void given_cpfFormatado_when_postCartoes_then_aceita() throws Exception {
        String requestBodyComCpfFormatado = """
                {
                    "cliente": {
                        "nome": "Cliente Teste",
                        "cpf": "123.456.789-10",
                        "idade": 25,
                        "data_nascimento": "2000-01-01",
                        "uf": "SP",
                        "renda_mensal": 4000,
                        "email": "cliente@teste.com",
                        "telefone_whatsapp": "11999992020"
                    }
                }
                """;

        when(cartaoService.solicitar(any())).thenReturn(criarResponseComCartoes());

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyComCpfFormatado))
                .andExpect(status().isOk());
    }

    // testa renda zero é aceita (não é negativa)
    @Test
    void given_rendaZero_when_postCartoes_then_naoRetorna400() throws Exception {
        when(cartaoService.solicitar(any())).thenReturn(criarResponseSemCartoes());

        String json = """
                {
                    "cliente": {
                        "nome": "João Silva",
                        "cpf": "12345678901",
                        "idade": 30,
                        "data_nascimento": "%s",
                        "uf": "RJ",
                        "renda_mensal": 0,
                        "email": "joao@email.com",
                        "telefone_whatsapp": "11999999999"
                    }
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());
    }

    /**
     * Teste para validar contrato de erro de validação 400 (campos faltando)
     */
    @Test
    void given_camposFaltando_when_postCartoes_then_retorna400ComContratoErro() throws Exception {
        String json = """
                {
                    "cliente": {
                        "nome": "João"
                    }
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.mensagem").value("Validação de entrada falhou"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("VALIDACAO_ENTRADA"))
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna").isNotEmpty());
    }
}
