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

@ExtendWith(MockitoExtension.class)
class CartoesControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartaoService cartaoService;

    @InjectMocks
    private CartoesController controller;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    private String validRequestJson() {
        return """
                {
                    "nome": "João Silva",
                    "cpf": "12345678901",
                    "data_nascimento": "%s",
                    "uf": "RJ",
                    "renda_mensal": 8000.00,
                    "email": "joao@email.com",
                    "telefone_whatsapp": "11999999999"
                }
                """.formatted(LocalDate.now().minusYears(30));
    }

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

    @Test
    void given_clienteSemCartoes_when_postCartoes_then_retorna204() throws Exception {
        when(cartaoService.solicitar(any())).thenReturn(criarResponseSemCartoes());

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isNoContent());
    }

    @Test
    void given_camposFaltando_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "nome": "João"
                }
                """;

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_emailInvalido_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "nome": "João Silva",
                    "cpf": "12345678901",
                    "data_nascimento": "%s",
                    "uf": "RJ",
                    "renda_mensal": 8000.00,
                    "email": "invalido",
                    "telefone_whatsapp": "11999999999"
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_rendaNegativa_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "nome": "João Silva",
                    "cpf": "12345678901",
                    "data_nascimento": "%s",
                    "uf": "RJ",
                    "renda_mensal": -1000,
                    "email": "joao@email.com",
                    "telefone_whatsapp": "11999999999"
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

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

    @Test
    void given_erroInterno_when_postCartoes_then_retorna500() throws Exception {
        when(cartaoService.solicitar(any())).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void given_bodyVazio_when_postCartoes_then_retorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_telefoneInvalido_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "nome": "João Silva",
                    "cpf": "12345678901",
                    "idade": 30,
                    "data_nascimento": "%s",
                    "uf": "RJ",
                    "renda_mensal": 8000.00,
                    "email": "joao@email.com",
                    "telefone_whatsapp": "123"
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void given_idadeNegativa_when_postCartoes_then_retorna400() throws Exception {
        String json = """
                {
                    "nome": "João Silva",
                    "cpf": "12345678901",
                    "idade": -1,
                    "data_nascimento": "%s",
                    "uf": "RJ",
                    "renda_mensal": 8000.00,
                    "email": "joao@email.com",
                    "telefone_whatsapp": "11999999999"
                }
                """.formatted(LocalDate.now().minusYears(30));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
