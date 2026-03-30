# Consideracoes do Projeto

## Decisoes de Arquitetura

### Strategy Pattern para Regras de Elegibilidade

A decisao mais importante do projeto foi usar o **Strategy Pattern** para as regras de elegibilidade. Cada regra (idade, renda, UF) e uma classe separada que implementa a interface `ElegibilidadeRule`.

**Por que essa escolha:**
- **Extensibilidade**: Para adicionar uma nova regra (ex: score de credito, historico), basta criar uma nova classe que implementa `ElegibilidadeRule` e anotá-la com `@Component`. O Spring injeta automaticamente na lista de regras.
- **Testabilidade**: Cada regra tem seu proprio teste unitario isolado, sem depender das outras.
- **Manutencao**: Alterar a logica de uma regra nao impacta as demais.
- **Sem if/else gigante**: Evita um metodo monolitico cheio de condicionais encadeados.

### Validacao em Camadas

O projeto valida dados em 3 camadas progressivas:

1. **Bean Validation (Jakarta)** - Validacoes sintaticas automaticas (`@NotBlank`, `@Email`, `@Min`, `@Pattern`). Retorna 400 automaticamente.
2. **Custom Service Validation (`ValidacaoClienteService`)** - Regras de negocio que precisam de logica (formato CPF, UF valida, coerencia idade/data nascimento). Retorna 400 ou 422.
3. **Regras de Elegibilidade** - Filtros de negocio aplicados sequencialmente. Se nenhum cartao passa, retorna 204.

**Vantagem**: Erros sao detectados o mais cedo possivel, evitando processamento desnecessario.

### RFC 9457 para Respostas de Erro

Todas as respostas de erro seguem o padrao RFC 9457 (Problem Details for HTTP APIs) com campos `codigo`, `mensagem` e `detalhe_erro`. Isso garante consistencia para os consumidores da API.

### Separacao DTO / Entity / Domain Model

- **DTOs** (`ClienteRequestDTO`, `SolicitacaoResponseDTO`): Contratos da API, anotados com Jackson/Validation
- **Entities** (`CartaoOferta`, `Solicitacao`): Mapeamento JPA, representam tabelas do banco
- **Domain Models** (`Cliente`, `ResultadoElegibilidade`): Logica de negocio pura, sem acoplamento

Essa separacao evita que mudancas na API afetem o banco e vice-versa.

### H2 em Memoria

Escolhi H2 por simplicidade e velocidade de desenvolvimento. Nao precisa de instalacao externa, os testes rodam isolados, e a aplicacao sobe instantaneamente. Para producao, seria trocado por PostgreSQL sem alterar o codigo (apenas configuracao).

---

## Java vs Kotlin

O desafio pedia **Kotlin**, mas optei por **Java 17** pois tenho mais familiaridade e confianca na linguagem. Priorizei entregar um codigo limpo, bem testado e bem estruturado em Java do que um codigo mediano em Kotlin.

**Pontos a considerar:**
- Java 17 oferece recursos modernos como text blocks, records e pattern matching
- Kotlin e Java rodam na mesma JVM e o Spring Boot suporta ambos igualmente
- A migracao para Kotlin seria relativamente simples pois a arquitetura esta desacoplada
- Lombok supre parcialmente o que Kotlin oferece nativamente (data classes, null safety)

Se fosse refazer, investiria tempo em aprender Kotlin antes para atender ao requisito original.

---

## O que Faria Diferente

### Com mais tempo

1. **Kotlin**: Migraria para Kotlin conforme pedido no desafio
2. **Configuracoes externalizadas**: Idade minima (18 anos), rendas minimas e limites dos cartoes viriam do `application.yml` ao inves de estarem hardcoded. Isso permitiria alterar regras sem recompilar.
3. **Testes de integracao end-to-end**: Usaria `@SpringBootTest` com `TestRestTemplate` para testar o fluxo completo (request HTTP -> controller -> service -> repository -> response)
4. **Cache**: As ofertas de cartao mudam raramente. Um cache (Spring Cache ou Redis) evitaria consultas repetidas ao banco.
5. **Paginacao e historico**: Endpoint GET para consultar solicitacoes anteriores por CPF, com paginacao.

### Decisoes que repensaria

- **Regras de idade nos boundaries**: O enunciado diz "maior que 18" e "maior que 25" (exclusivo). Inicialmente implementei como `>= 18` e `>= 25`, mas corrigi para `> 18` e `> 25` para seguir o enunciado fielmente.
- **Persistencia da solicitacao**: Hoje persiste mesmo quando nenhum cartao e elegivel (204). Poderia condicionar a persistencia apenas para solicitacoes com cartoes aprovados.

---

## Sugestoes de Evolucao

### Curto prazo
- **Configuracoes dinamicas**: Extrair valores de regras (idade minima, rendas, limites) para `application.yml` com `@ConfigurationProperties`
- **Metricas**: Integrar Micrometer + Prometheus para monitorar tempo de resposta, taxa de aprovacao/rejeicao, cartoes mais ofertados
- **CI/CD**: Pipeline com GitHub Actions para build, testes e deploy automatico
- **Versionamento da API**: Adicionar `/v1/cartoes` para facilitar evolucoes futuras sem quebrar consumidores

### Medio prazo
- **Banco relacional real**: Migrar para PostgreSQL com Flyway para migrations
- **Autenticacao**: OAuth2/JWT para proteger a API

### Longo prazo
- **Event sourcing**: Registrar cada decisao de elegibilidade como evento para auditoria completa
- **Microservicos**: Separar validacao, elegibilidade e notificacao em servicos independentes
- **Machine Learning**: Score de credito baseado em historico para complementar as regras fixas
- **Multi-tenancy**: Suportar diferentes instituicoes com regras customizadas

---

## Trade-offs Conscientes

| Decisao | Vantagem | Desvantagem |
|---------|----------|-------------|
| H2 em memoria | Zero config, testes rapidos | Dados perdem-se ao reiniciar |
| Strategy Pattern | Extensivel, testavel | Mais classes que um if/else simples |
| Bean Validation + Custom | Erros claros por camada | Duplicacao parcial de validacoes |
| Lombok | Menos boilerplate | Dependencia adicional, magia de compilacao |
| Docker multi-stage | Imagem leve (~180MB) | Build mais lento que single-stage |
| RFC 9457 para erros | Padrao REST moderno | Mais verboso que mensagens simples |

---

## Conclusao

O projeto foi desenvolvido priorizando **qualidade de codigo**, **testabilidade** e **extensibilidade**. A arquitetura baseada em Strategy Pattern permite que novas regras de elegibilidade sejam adicionadas sem modificar codigo existente, seguindo o principio Open/Closed do SOLID.

Os 83 testes unitarios cobrem cenarios felizes e de erro, garantindo confianca para evolucoes futuras. A containerizacao com Docker, o health check via Actuator e a documentacao Swagger tornam a aplicacao pronta para um ambiente produtivo.

Reconheco que usar Java ao inves de Kotlin foi um desvio do requisito, mas acredito que a qualidade da entrega compensa essa escolha. Estou motivada a aprender Kotlin e aplica-lo em projetos futuros.
