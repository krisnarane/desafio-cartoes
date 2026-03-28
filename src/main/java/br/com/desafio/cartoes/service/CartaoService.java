package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.CartaoResponseDTO;
import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoResponseDTO;
import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.enums.StatusOferta;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.ResultadoElegibilidade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CartaoService {
    
    private final ValidacaoClienteService validacaoService;
    private final ElegibilidadeService elegibilidadeService;
    
    public SolicitacaoResponseDTO solicitar(ClienteRequestDTO clienteDTO) {
        log.info("Iniciando processamento de solicitação para CPF: {}", clienteDTO.getCpf());
        
        // Valida regras de negócio (lança ClienteInvalidoException se falhar)
        validacaoService.validar(clienteDTO);
        
        // Converte para modelo de domain
        Cliente cliente = validacaoService.converterParaModelo(clienteDTO);
        
        // Processa elegibilidade (aplica todas as rules)
        ResultadoElegibilidade resultadoElegibilidade = elegibilidadeService.processar(cliente);

        // Converte CartaoOferta para CartaoResponseDTO
        List<CartaoResponseDTO> cartoesResponse = resultadoElegibilidade.getCartoesAprovados().stream()
            .map(this::converterParaResponse)
            .toList();
        
        // Monta resposta
        SolicitacaoResponseDTO resposta = SolicitacaoResponseDTO.builder()
            .numeroSolicitacao(UUID.randomUUID().toString())
            .dataSolicitacao(LocalDateTime.now())
            .cliente(clienteDTO)
            .cartoesOfertados(cartoesResponse)
            .build();
        
        log.info("Solicitação processada para CPF {}: {} cartões aprovados", 
            clienteDTO.getCpf(), cartoesResponse.size());
        
        return resposta;
    }
    
    
     // Converter CartaoOferta para CartaoResponseDTO
    private CartaoResponseDTO converterParaResponse(CartaoOferta cartao) {
        return CartaoResponseDTO.builder()
            .tipoCartao(cartao.getTipoCartao().name())
            .valorAnuidadeMensal(cartao.getValorAnuidadeMensal())
            .valorLimiteDisponivel(cartao.getValorLimiteDisponivel())
            .status(StatusOferta.APROVADO.name())
            .build();
    }
}
