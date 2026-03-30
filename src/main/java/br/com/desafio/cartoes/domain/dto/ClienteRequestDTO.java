package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    @JsonProperty("nome")
    private String nome;
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$", message = "CPF deve estar formatado (123.456.789-10) ou conter 11 dígitos")
    @JsonProperty("cpf")
    private String cpf;
    
    @NotNull(message = "Idade é obrigatória")
    @Min(value = 18, message = "Cliente deve ter no mínimo 18 anos")
    @JsonProperty("idade")
    private Integer idade;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    @PastOrPresent(message = "Data de nascimento não pode ser no futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("data_nascimento")
    private LocalDate dataNascimento;
    
    @NotBlank(message = "UF é obrigatória")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ser uma sigla válida (ex: SP, RJ)")
    @JsonProperty("uf")
    private String uf;
    
    @NotNull(message = "Renda mensal é obrigatória")
    @PositiveOrZero(message = "Renda mensal não deve ser negativa")
    @JsonProperty("renda_mensal")
    private BigDecimal rendaMensal;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @JsonProperty("email")
    private String email;
    
    @NotBlank(message = "Telefone WhatsApp é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "Telefone deve ter 11 dígitos")
    @JsonProperty("telefone_whatsapp")
    private String telefoneWhatsapp;
}
