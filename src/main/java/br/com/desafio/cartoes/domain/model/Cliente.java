package br.com.desafio.cartoes.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String uf;
    private BigDecimal rendaMensal;
    private String email;
    private String telefoneWhatsapp;
    
    /**
     * Calcula a idade do cliente baseado na data de nascimento
     */
    public int calcularIdade() {
        return Period.between(this.dataNascimento, LocalDate.now()).getYears();
    }
}
