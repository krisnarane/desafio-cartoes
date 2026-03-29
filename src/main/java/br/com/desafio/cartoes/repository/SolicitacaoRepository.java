package br.com.desafio.cartoes.repository;

import br.com.desafio.cartoes.domain.entity.Solicitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends JpaRepository<Solicitacao, Long> {
    Optional<Solicitacao> findByNumeroSolicitacao(String numeroSolicitacao);
}
