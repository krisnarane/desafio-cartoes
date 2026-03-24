package br.com.desafio.cartoes.domain.model;

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
public class Cliente {
    
    private String nome;
    private String cpf;
    private Integer idade;
    private LocalDate dataNascimento;
    private String uf;
    private BigDecimal rendaMensal;
    private String email;
    private String telefoneWhatsapp;
}
