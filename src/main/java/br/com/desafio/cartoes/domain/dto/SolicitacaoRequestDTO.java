package br.com.desafio.cartoes.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoRequestDTO {

    @NotNull(message = "O campo 'cliente' é obrigatório")
    @Valid
    @JsonProperty("cliente")
    private ClienteRequestDTO cliente;
}
