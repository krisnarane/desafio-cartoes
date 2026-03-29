package br.com.desafio.cartoes.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "numero_solicitacao", unique = true, nullable = false, length = 36)
    private String numeroSolicitacao;
    
    @Column(name = "cpf_cliente", nullable = false, length = 20)
    private String cpfCliente;
    
    @Column(name = "data_solicitacao", nullable = false)
    private LocalDateTime dataSolicitacao;
    
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
    
    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
