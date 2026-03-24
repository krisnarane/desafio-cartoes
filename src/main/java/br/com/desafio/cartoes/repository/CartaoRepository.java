package br.com.desafio.cartoes.repository;

import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartaoRepository extends JpaRepository<CartaoOferta, Long> {
    
    List<CartaoOferta> findByAtivoTrue();
}
