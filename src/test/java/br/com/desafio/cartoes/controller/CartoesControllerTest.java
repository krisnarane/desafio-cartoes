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
                .andExpect(jsonPath("$.detail").value("Cliente menor de 18 anos não é elegível"));
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
                .andExpect(jsonPath("$.detail").value("CPF formato inválido"));
    }

    // testa erro interno, 500
    @Test
    void given_erroInterno_when_postCartoes_then_retorna500() throws Exception {
        when(cartaoService.solicitar(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isInternalServerError());
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
}
