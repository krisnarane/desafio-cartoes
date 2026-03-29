package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.exception.ClienteInvalidoException;
import br.com.desafio.cartoes.util.ClienteValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        
        // Valida idade: deve ser >= 18
        if (dto.getIdade() < 18) {
            log.warn("Cliente menor de 18 anos rejeitado: CPF {}, idade {}", dto.getCpf(), dto.getIdade());
            throw new ClienteInvalidoException(
                "Cliente menor de 18 anos não é elegível",
                "CLIENTE_MENOR_DE_IDADE",
                422
            );
        }
        
        // Valida coerência entre idade informada e data de nascimento (segurança adicional)
        LocalDate agora = LocalDate.now();
        int idadeCalculada = Period.between(dto.getDataNascimento(), agora).getYears();
        
        if (Math.abs(dto.getIdade() - idadeCalculada) > 1) {
            log.warn("Idade incoerente: informada {}, calculada {}", dto.getIdade(), idadeCalculada);
            throw new ClienteInvalidoException(
                "Idade informada não corresponde à data de nascimento",
                "IDADE_INCOERENTE",
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
