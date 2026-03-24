package br.com.desafio.cartoes.domain.entity;

import br.com.desafio.cartoes.domain.enums.TipoCartao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cartao_oferta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartaoOferta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCartao tipoCartao;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal rendaMinima;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal valorAnuidadeMensal;
    
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal valorLimiteDisponivel;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
