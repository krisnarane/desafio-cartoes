package br.com.desafio.cartoes.service;

import br.com.desafio.cartoes.domain.dto.CartaoResponseDTO;
import br.com.desafio.cartoes.domain.dto.ClienteRequestDTO;
import br.com.desafio.cartoes.domain.dto.SolicitacaoResponseDTO;
import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import br.com.desafio.cartoes.domain.entity.Solicitacao;
import br.com.desafio.cartoes.domain.enums.StatusOferta;
import br.com.desafio.cartoes.domain.enums.TipoCartao;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.ResultadoElegibilidade;
import br.com.desafio.cartoes.repository.SolicitacaoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class CartaoService {
    
    private final ValidacaoClienteService validacaoService;
    private final ElegibilidadeService elegibilidadeService;
    private final SolicitacaoRepository solicitacaoRepository;
    
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
        
        // Gera número único de solicitação
        String numeroSolicitacao = UUID.randomUUID().toString();
        LocalDateTime dataSolicitacao = LocalDateTime.now();
        
        // Monta resposta
        SolicitacaoResponseDTO resposta = SolicitacaoResponseDTO.builder()
            .numeroSolicitacao(numeroSolicitacao)
            .dataSolicitacao(dataSolicitacao)
            .cliente(clienteDTO)
            .cartoesOfertados(cartoesResponse)
            .build();
        
        // Persiste solicitação para auditoria
        Solicitacao solicitacaoEntity = Solicitacao.builder()
            .numeroSolicitacao(numeroSolicitacao)
            .cpfCliente(clienteDTO.getCpf())
            .dataSolicitacao(dataSolicitacao)
            .build();
        
        solicitacaoRepository.save(solicitacaoEntity);
        log.info("Solicitação {} salva no banco de dados para CPF: {} com {} cartões aprovados", 
            numeroSolicitacao, clienteDTO.getCpf(), cartoesResponse.size());
        
        return resposta;
    }
    
    
     // Converter CartaoOferta para CartaoResponseDTO
    private CartaoResponseDTO converterParaResponse(CartaoOferta cartao) {
        // CARTAO_SEM_ANUIDADE sempre retorna valor 0.00 conforme especificação
        BigDecimal valorAnuidade = TipoCartao.CARTAO_SEM_ANUIDADE == cartao.getTipoCartao()
            ? BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP)
            : cartao.getValorAnuidadeMensal();
        
        return CartaoResponseDTO.builder()
            .tipoCartao(cartao.getTipoCartao().name())
            .valorAnuidadeMensal(valorAnuidade)
            .valorLimiteDisponivel(cartao.getValorLimiteDisponivel())
            .status(StatusOferta.APROVADO.name())
            .build();
    }
}
