package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.exception.ClienteInvalidoException;
import br.com.desafio.cartoes.util.ClienteValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Service
@Slf4j
public class ValidacaoClienteService {
    
    /**
     * Valida regras de negócio do cliente
     * Lança ClienteInvalidoException se as regras forem violadas
     */
    public void validar(ClienteRequestDTO dto) {
        log.debug("Iniciando validação do cliente: {}", dto.getCpf());
        
        // Valida CPF
        if (!ClienteValidator.isValidCPFFormat(dto.getCpf())) {
            log.warn("CPF inválido: {}", dto.getCpf());
            throw new ClienteInvalidoException(
                "CPF formato inválido",
                "CPF_INVALIDO",
                400
            );
        }
        
        // Valida UF
        if (!ClienteValidator.isValidUF(dto.getUf())) {
            log.warn("UF inválida: {}", dto.getUf());
            throw new ClienteInvalidoException(
                "UF inválida",
                "UF_INVALIDA",
                400
            );
        }
        
        // Valida idade
        LocalDate agora = LocalDate.now();
        int idade = Period.between(dto.getDataNascimento(), agora).getYears();
        
        if (idade < 18) {
            log.warn("Cliente menor de 18 anos rejeitado: CPF {}, idade {}", dto.getCpf(), idade);
            throw new ClienteInvalidoException(
                "Cliente menor de 18 anos não é elegível",
                "CLIENTE_MENOR_DE_IDADE",
                422
            );
        }
        
        // Valida renda (complementa Bean Validation)
        if (dto.getRendaMensal().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Renda inválida: {}", dto.getRendaMensal());
            throw new ClienteInvalidoException(
                "Renda mensal deve ser maior que zero",
                "RENDA_INVALIDA",
                400
            );
        }
        
        log.debug("Validação do cliente {} passou com sucesso", dto.getCpf());
    }
    
    /**
     * Converte ClienteRequestDTO para Model Cliente
     */
    public Cliente converterParaModelo(ClienteRequestDTO dto) {
        return Cliente.builder()
            .nome(dto.getNome())
            .cpf(dto.getCpf())
            .dataNascimento(dto.getDataNascimento())
            .uf(dto.getUf())
            .rendaMensal(dto.getRendaMensal())
            .email(dto.getEmail())
            .telefoneWhatsapp(dto.getTelefoneWhatsapp())
            .build();
    }
}
