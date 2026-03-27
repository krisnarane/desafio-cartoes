# 💳 Cartões - API de Elegibilidade de Cartões de Crédito

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/technologies/javase/jdk17-archive.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.4-green?logo=spring)](https://spring.io/projects/spring-boot)
[![H2 Database](https://img.shields.io/badge/Database-H2-blue?logo=database)](https://www.h2database.com/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-blue?logo=docker)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

**Sistema inteligente de elegibilidade para cartões de crédito** com validações em camadas, regras de negócio extensíveis, logging auditável e containerização production-ready. Desenvolvido como desafio técnico para o Itaú.

## 📑 Índice

- [Visão Geral](#visão-geral)
- [Quick Start](#quick-start)
- [Arquitetura](#arquitetura)
- [API Completa](#api-completa)
- [Regras de Elegibilidade](#regras-de-elegibilidade)
- [Extensibilidade](#extensibilidade)
- [Testes](#testes)
- [Logging e Auditoria](#logging-e-auditoria)
- [Docker e Deployment](#docker-e-deployment)
- [Performance e Escalabilidade](#performance-e-escalabilidade)
- [Troubleshooting](#troubleshooting)
- [Referência de Erros](#referência-de-erros)

---

## 🎯 Visão Geral

### O que é?

Uma **API REST que determina automaticamente quais cartões de crédito um cliente se qualifica**, baseado em:
- ✅ **Idade** (18+ anos obrigatório)
- ✅ **Renda mensal** (cada cartão tem mínimo)
- ✅ **Estado (UF)** (São Paulo tem regras especiais)
- ✅ **Validação de dados** (CPF, email, formato)

### Por que importa?

Em instituições financeiras como Itaú:
- **Rastreabilidade**: Cada decisão fica registrada para auditoria regulatória
- **Compliance**: Segue regulações do Banco Central e INTERPOL
- **Fraude**: Detecta padrões suspeitos em logs
- **Escalabilidade**: Suporta múltiplas solicitações simultâneas
- **Extensibilidade**: Adicione novas cartões e regras sem refatorar

### Stack & Padrões Utilizados

| Aspecto | Escolha | Justificativa |
|---------|---------|---------------|
| **Linguagem** | Java 17 (LTS) | Tipo forte, segurança, performance |
| **Framework** | Spring Boot 4.0.4 | Produção-ready, comunidade vasta, Actuator |
| **Padrão de Regras** | Strategy Pattern | Extensível, testável, sem if/else gigante |
| **Banco de Dados** | H2 em-memória | Desenvolvimento rápido, testes isolados |
| **Validação** | Camadas (Bean Validation + Custom) | Prevenção de dados inválidos cedo |
| **Erro** | RFC 9457 (Problem Details) | Padrão REST moderno e consistente |
| **Logging** | SLF4J + Logback | Auditoria completa, rastreabilidade |
| **Container** | Docker Multi-stage | Imagem leve (~180MB), segura, otimizada |

---

## 🚀 Quick Start

### Pré-requisitos

- Java 17+
- Maven 3.9+
- Docker (opcional, para container)

### Desenvolvimento (Maven + H2 em-memória) ⭐ Recomendado

```bash
# 1. Clonar repositório
cd cartoes

# 2. Compilar
mvn clean compile

# 3. Executar aplicação (Profile DEV ativado por padrão)
mvn spring-boot:run

# 4. Rodar testes (em outro terminal)
mvn test
```

✅ **Dev Profile (ativado automaticamente):**
- ❌ Docker Compose NÃO sobe
- ✅ Apenas Maven na porta 8080
- ✅ H2 banco em-memória
- ✅ Logging DEBUG ativado

**A aplicação estará acessível em:**

| Recurso | URL |
|---------|-----|
| **API REST** | http://localhost:8080 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI Spec** | http://localhost:8080/v3/api-docs |
| **H2 Console** | http://localhost:8080/h2-console |
| **Health Check** | http://localhost:8080/actuator/health |

### Produção (Docker Compose) 🐳

```bash
# Build com profile PROD (inclui spring-boot-docker-compose)
mvn clean package -P prod

# Subir container
docker-compose up -d

# Ver logs em tempo real
docker-compose logs -f cartoes-api

# Parar
docker-compose down
```

✅ **Prod Profile:**
- ✅ Docker Compose sobe junto
- ✅ Apenas Docker na porta 8080
- ✅ Logging INFO (menos verbose)
- ✅ Otimizado para produção

---

## ⚙️ Maven Profiles

O projeto utiliza **2 profiles Maven** para separar desenvolvimento de produção:

### Profile: `dev` (Padrão)

**Ativado automaticamente** quando você executa `mvn spring-boot:run` sem especificar `-P`.

```bash
# Equivalente a:
mvn spring-boot:run -P dev
```

**Características:**

| Aspecto | Configuração |
|---------|-------------|
| **Docker Compose** | ❌ Desativado (`spring.docker.compose.enabled=false`) |
| **Banco de Dados** | H2 em-memória (`application-dev.yml`) |
| **Logging Level** | DEBUG (detalhado) |
| **Port** | 8080 (apenas Maven) |
| **Use Case** | Desenvolvimento local, testes, debugging |

**application-dev.yml:**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:cartoes
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
  h2:
    console:
      enabled: true
      
logging:
  level:
    br.com.desafio.cartoes: DEBUG
```

### Profile: `prod` (Produção)

**Ativado explicitamente:**

```bash
mvn clean package -P prod
docker-compose up
```

**Características:**

| Aspecto | Configuração |
|---------|-------------|
| **Docker Compose** | ✅ Ativado (dependência adicionada) |
| **Banco de Dados** | Pode ser externo (MySQL, PostgreSQL, etc.) |
| **Logging Level** | INFO (menos verbose) |
| **Port** | 8080 (docker compose) |
| **Use Case** | Deploy em produção, staging, CI/CD |

**application-prod.yml:**
```yaml
spring:
  application:
    name: cartoes
  jackson:
    property-naming-strategy: SNAKE_CASE
    
logging:
  level:
    root: INFO
    br.com.desafio.cartoes: INFO
```

### Por que Profiles?

❌ **Antes (Problema):**
```bash
mvn spring-boot:run
# Docker sobe JUNTO
# Maven + Docker na mesma porta 8080 = CONFLITO! 💥
```

✅ **Depois (Solução):**
```bash
mvn spring-boot:run    # Dev: Maven apenas, sem Docker
docker-compose up      # Prod: Docker apenas, sem Maven
```

---

## 🏗️ Arquitetura

### Fluxo de Requisição

```
┌────────────────────────────────────────────────────────────────┐
│ 1. CLIENT: POST /cartoes com dados do cliente                 │
└────────────────────────────────┬───────────────────────────────┘
                                 ↓
┌────────────────────────────────────────────────────────────────┐
│ 2. CONTROLLER: CartoesController valida estrutura da request   │
└────────────────────────────────┬───────────────────────────────┘
                                 ↓
┌────────────────────────────────────────────────────────────────┐
│ 3. VALIDAÇÃO: ValidacaoClienteService verifica:               │
│    • Formato CPF (11 dígitos)                                  │
│    • Estado válido (UF)                                        │
│    • Idade ≥ 18 anos                                           │
│    • Renda > 0                                                 │
└────────────────────────────────┬───────────────────────────────┘
                                 ↓ (válido)
┌────────────────────────────────────────────────────────────────┐
│ 4. ORQUESTRAÇÃO: CartaoService busca todas as ofertas de       │
│    cartões do repositório                                      │
└────────────────────────────────┬───────────────────────────────┘
                                 ↓
┌────────────────────────────────────────────────────────────────┐
│ 5. ELEGIBILIDADE: ElegibilidadeService aplica regras em        │
│    sequência com padrão Strategy:                              │
│                                                                │
│    Início: [todos os 3 cartões]                               │
│       ↓                                                         │
│    RegraPorIdade (filtra por idade)                            │
│       ↓                                                         │
│    RegraPorRenda (filtra por renda mínima)                    │
│       ↓                                                         │
│    RegraPorUF (aplica regras do estado)                       │
│       ↓                                                         │
│    Resultado: [cartões elegíveis] ou []                        │
└────────────────────────────────┬───────────────────────────────┘
                                 ↓
┌────────────────────────────────────────────────────────────────┐
│ 6. RESPONSE:                                                   │
│    • 200 OK + lista de cartões (se houver elegíveis)          │
│    • 204 No Content (se nenhum elegível)                       │
│    • 400/422 Erro (validação ou regra de negócio)             │
└────────────────────────────────────────────────────────────────┘
```

### Camadas da Arquitetura

```
┌─────────────────────────────────────────┐
│   CartoesController                     │ ← REST Endpoint
│   │                                     │
├───┼─────────────────────────────────────┤
│   └→ CartaoService                      │ ← Orquestração
│       │                                 │
├───────┼─────────────────────────────────┤
│       ├→ ValidacaoClienteService        │ ← Validação de dados
│       │                                 │
│       └→ ElegibilidadeService           │ ← Aplicação de regras
│           │                             │
├───────────┼─────────────────────────────┤
│           ├→ RegraPorIdade              │ ← Implementação de
│           ├→ RegraPorRenda              │   ElegibilidadeRule
│           └→ RegraPorUF                 │   (Strategy Pattern)
│                                         │
├─────────────────────────────────────────┤
│   CartaoRepository (JPA)                │ ← Acesso a dados
│                                         │
├─────────────────────────────────────────┤
│   H2 Database (CartaoOferta)            │ ← Persistência
└─────────────────────────────────────────┘

GlobalExceptionHandler ← Trata erros em cada camada
```

### Componentes-Chave

| Classe | Responsabilidade | Padrão |
|--------|------------------|--------|
| **CartoesController** | Expõe endpoint REST `/cartoes` | MVC |
| **CartaoService** | Orquestra validação e elegibilidade | Facade |
| **ValidacaoClienteService** | Valida dados do cliente | Validator |
| **ElegibilidadeService** | Aplica regras em sequência | Chain of Strategy |
| **ElegibilidadeRule** (interface) | Contrato para regras de negócio | Strategy |
| **RegraPor*** | Implementações específicas de regras | Strategy |
| **CartaoRepository** | CRUD de cartões | DAO |
| **GlobalExceptionHandler** | Tratamento centralizado de erros | Exception Handler |

### Banco de Dados

**Entity: CartaoOferta**

```
┌──────────────────────────────────┐
│      CartaoOferta                │
├──────────────────────────────────┤
│ id: Long (PK, auto-increment)    │
│ tipoCartao: TipoCartao (ENUM)    │
│ rendaMinima: BigDecimal          │
│ valorAnuidadeMensal: BigDecimal  │
│ valorLimiteDisponivel: BigDecimal│
│ ativo: Boolean (soft delete)     │
└──────────────────────────────────┘
```

**Dados Iniciais (3 cartões pré-carregados):**

| Tipo | Renda Mínima | Anuidade | Limite | Descrição |
|------|-------------|----------|--------|-----------|
| `CARTAO_SEM_ANUIDADE` | R$ 3.500 | R$ 0 | R$ 1.000 | Cartão básico, sem taxa |
| `CARTAO_DE_PARCEIROS` | R$ 5.500 | R$ 10/mês | R$ 3.000 | Parcerias e benefícios |
| `CARTAO_COM_CASHBACK` | R$ 7.500 | R$ 20/mês | R$ 5.000 | Cashback em compras |

---

## 📞 API Completa

### POST /cartoes - Solicitar Elegibilidade

Avalia elegibilidade do cliente para cartões de crédito.

#### Request Headers
```
Content-Type: application/json
```

#### Request Body
```json
{
  "nome": "João Silva",
  "cpf": "12345678901",
  "data_nascimento": "1996-05-15",
  "uf": "RJ",
  "renda_mensal": 6000.00,
  "email": "joao@email.com",
  "telefone_whatsapp": "21987654321"
}
```

#### Request Fields

| Campo | Tipo | Validação | Exemplo |
|-------|------|-----------|---------|
| `nome` | String | 1-200 caracteres | "João Silva" |
| `cpf` | String | 11 dígitos ou XXX.XXX.XXX-XX | "12345678901" |
| `data_nascimento` | String (ISO 8601) | Deve ter ≥18 anos | "1996-05-15" |
| `uf` | String | 2 letras maiúsculas | "SP", "RJ", "MG" |
| `renda_mensal` | BigDecimal | > 0 | 6000.00 |
| `email` | String | Email válido | "joao@email.com" |
| `telefone_whatsapp` | String | Formato brasileiro | "21987654321" |

#### Response: 200 OK (Cartões Aprovados)
```json
{
  "numero_solicitacao": "a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6",
  "data_solicitacao": "2026-03-26T14:30:00.123",
  "cliente": {
    "nome": "João Silva",
    "cpf": "123.456.789-01",
    "data_nascimento": "1996-05-15",
    "uf": "RJ",
    "renda_mensal": 6000.00,
    "email": "joao@email.com",
    "telefone_whatsapp": "21987654321"
  },
  "cartoes_ofertados": [
    {
      "tipo_cartao": "CARTAO_SEM_ANUIDADE",
      "valor_anuidade_mensal": 0.00,
      "valor_limite_disponivel": 1000.00,
      "status": "APROVADO"
    },
    {
      "tipo_cartao": "CARTAO_DE_PARCEIROS",
      "valor_anuidade_mensal": 10.00,
      "valor_limite_disponivel": 3000.00,
      "status": "APROVADO"
    }
  ]
}
```

#### Response: 204 No Content (Sem Elegibilidade)
Nenhum cartão elegível para este cliente.

#### Response: 400 Bad Request (Validação de Formato)
```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "CPF deve conter 11 dígitos",
  "codigo_erro": "CPF_INVALIDO"
}
```

#### Response: 422 Unprocessable Entity (Regra de Negócio)
```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Cliente deve ter 18 anos ou mais",
  "codigo_erro": "CLIENTE_MENOR_DE_IDADE"
}
```

#### Response: 500 Internal Server Error
```json
{
  "type": "about:blank",
  "title": "Internal Server Error",
  "status": 500,
  "detail": "Erro inesperado ao processar solicitação",
  "codigo_erro": "ERRO_INTERNO"
}
```

### Testando com cURL

**Caso 1: Sucesso (cliente elegível para 2 cartões)**
```bash
curl -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João da Silva",
    "cpf": "12345678901",
    "data_nascimento": "1996-05-15",
    "uf": "SP",
    "renda_mensal": 8000.00,
    "email": "joao@email.com",
    "telefone_whatsapp": "11987654321"
  }'
```

**Caso 2: Validação falha (CPF inválido)**
```bash
curl -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João",
    "cpf": "123",
    "data_nascimento": "1996-05-15",
    "uf": "SP",
    "renda_mensal": 5000.00,
    "email": "joao@email.com",
    "telefone_whatsapp": "11987654321"
  }'
```

**Caso 3: Regra violada (menor de 18 anos)**
```bash
curl -X POST http://localhost:8080/cartoes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria",
    "cpf": "12345678901",
    "data_nascimento": "2010-05-15",
    "uf": "RJ",
    "renda_mensal": 5000.00,
    "email": "maria@email.com",
    "telefone_whatsapp": "21987654321"
  }'
```

---

## 🔐 Regras de Elegibilidade

As regras são aplicadas em **sequência** (Pipeline). Se um cartão não pass

a em uma regra, é descartado automaticamente.

### 1️⃣ RegraPorIdade

Filtra cartões baseado na **idade do cliente**.

| Faixa Etária | Cartões Elegíveis |
|-------------|-------------------|
| 18-24 anos | ✓ `CARTAO_SEM_ANUIDADE` |
| ≥ 25 anos | ✓ Todos os 3 cartões |

**Exemplo:**
- Cliente com 22 anos → apenas cartão sem anuidade
- Cliente com 30 anos → todos os 3 cartões

### 2️⃣ RegraPorRenda

Filtra cartões onde a **renda mensal do cliente ≥ renda mínima do cartão**.

| Cartão | Renda Mínima | Filtragem |
|--------|-------------|----------|
| `CARTAO_SEM_ANUIDADE` | R$ 3.500 | cliente_renda ≥ 3500 |
| `CARTAO_DE_PARCEIROS` | R$ 5.500 | cliente_renda ≥ 5500 |
| `CARTAO_COM_CASHBACK` | R$ 7.500 | cliente_renda ≥ 7500 |

**Exemplo:**
- Cliente renda R$ 4.000 → elegível para cartão sem anuidade apenas
- Cliente renda R$ 8.000 → elegível para todos os 3

### 3️⃣ RegraPorUF

Aplica regras **específicas do estado** (São Paulo tem politica especial).

#### São Paulo (SP)
```
Se idade 25-29: ✓ Todos os 3 cartões
Se idade < 25:  ✓ CARTAO_SEM_ANUIDADE apenas
Se idade ≥ 30:  ✓ CARTAO_SEM_ANUIDADE + CARTAO_COM_CASHBACK
                  (remove CARTAO_DE_PARCEIROS)
```

#### Outros Estados
```
✓ Todos os 3 cartões (sem restrições adicionais)
```

**Exemplo:**
- SP, 26 anos → todos os 3
- SP, 32 anos → remove de Parceiros (fica 2)
- RJ, 22 anos → mantém o que passou nas regras anteriores

### Ordem de Aplicação (Importante!)

```java
// Aplicadas NESSA ORDEM no ElegibilidadeService
List<CartaoOferta> cartoes = loadCartoes(); // [3 cartões inicialmente]

cartoes = regraPorIdade.aplicar(cliente, cartoes);    // Filtra 1-3
cartoes = regraPorRenda.aplicar(cliente, cartoes);    // Filtra mais
cartoes = regraPorUF.aplicar(cliente, cartoes);       // Filtra final

return cartoes; // Resultado final
```

**Implicação:** Uma regra anterior afeta as próximas.

---

## 🏗️ Extensibilidade

### Adicionar Nova Regra (Exemplo: RegraPorScore de Crédito)

#### Passo 1: Criar classe que implementa `ElegibilidadeRule`

```java
package br.com.desafio.cartoes.rule;

import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.entity.CartaoOferta;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Component
@Slf4j
public class RegraPorScore implements ElegibilidadeRule {
    
    @Override
    public List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes) {
        log.debug("Aplicando RegraPorScore para CPF: {}", cliente.getCpf());
        
        Integer score = obterScoreDoCliente(cliente.getCpf());
        
        return cartoes.stream()
            .filter(c -> score >= obterScoreMinimoParaCartao(c))
            .toList();
    }
    
    private Integer obterScoreDoCliente(String cpf) {
        // Chamar serviço externo ou banco de dados
        return 750; // exemplo
    }
    
    private Integer obterScoreMinimoParaCartao(CartaoOferta c) {
        return switch(c.getTipoCartao()) {
            case CARTAO_SEM_ANUIDADE -> 500;
            case CARTAO_DE_PARCEIROS -> 650;
            case CARTAO_COM_CASHBACK -> 700;
        };
    }
}
```

#### Passo 2: Injetar no `ElegibilidadeService`

```java
@Service
@Slf4j
public class ElegibilidadeService {
    
    private final RegraPorIdade regraPorIdade;
    private final RegraPorRenda regraPorRenda;
    private final RegraPorUF regraPorUF;
    private final RegraPorScore regraPorScore; // ← NOVA
    
    public ElegibilidadeService(
        RegraPorIdade ri, RegraPorRenda rr, RegraPorUF ruf,
        RegraPorScore rs) {
        this.regraPorIdade = ri;
        this.regraPorRenda = rr;
        this.regraPorUF = ruf;
        this.regraPorScore = rs;
    }
    
    public List<CartaoOferta> processar(Cliente cliente, List<CartaoOferta> cartoes) {
        cartoes = regraPorIdade.aplicar(cliente, cartoes);
        cartoes = regraPorRenda.aplicar(cliente, cartoes);
        cartoes = regraPorUF.aplicar(cliente, cartoes);
        cartoes = regraPorScore.aplicar(cliente, cartoes); // ← APLICAR AQUI
        return cartoes;
    }
}
```

#### Passo 3: Adicionar testes

```java
@DisplayName("RegraPorScore - Teste de crédito")
class RegraPorScoreTest {
    
    private RegraPorScore regra;
    
    @BeforeEach
    void setUp() {
        regra = new RegraPorScore();
    }
    
    @Test
    void given_scoreAlto_when_aplicar_then_permiteCartaosCashback() {
        Cliente cliente = TestFactory.clienteComScore(800);
        List<CartaoOferta> cartoes = TestFactory.todosOsCartoes();
        
        List<CartaoOferta> resultado = regra.aplicar(cliente, cartoes);
        
        assertThat(resultado).hasSize(3);
    }
}
```

### Adicionar Novo Tipo de Cartão

#### Passo 1: Adicionar enum em `TipoCartao.java`

```java
public enum TipoCartao {
    CARTAO_SEM_ANUIDADE,
    CARTAO_DE_PARCEIROS,
    CARTAO_COM_CASHBACK,
    CARTAO_PREMIUM // ← NOVO
}
```

#### Passo 2: Adicionar dados iniciais em `DadosIniciais.java`

```java
@Component
public class DadosIniciais {
    
    public CommandLineRunner inserirCartoes(CartaoRepository repo) {
        return args -> {
            repo.saveAll(List.of(
                // ... cartões existentes ...
                new CartaoOferta(
                    TipoCartao.CARTAO_PREMIUM,
                    new BigDecimal("15000.00"),   // renda mínima
                    new BigDecimal("50.00"),      // anuidade
                    new BigDecimal("10000.00")    // limite
                )
            ));
        };
    }
}
```

#### Passo 3: Atualizar regras se necessário

Cada `ElegibilidadeRule` usa `switch(tipo)` ou `map`, então adicione o novo caso.

---

## 🧪 Testes

### Cobertura de Testes

O projeto possui **8 test suites** cobrindo todas as camadas:

| Classe de Teste | Foco | Testes |
|-----------------|------|--------|
| `CartoesControllerTest` | REST endpoint | 200, 204, 400, 422, 500 |
| `CartaoServiceTest` | Orquestração | Happy path, erros |
| `ElegibilidadeServiceTest` | Encadeamento de regras | Sequência, vazios |
| `RegraPorIdadeTest` | Idade | 18-24, 25+, limites |
| `RegraPorRendaTest` | Renda | Mínimos, 3 cartões |
| `RegraPorUFTest` | Estado | SP especial, outros |
| `ValidacaoClienteServiceTest` | Validações | CPF, UF, idade, renda |
| `ClienteValidatorTest` | Utilitários | Formatos |

### Executar Testes

```bash
# Todos
mvn test

# Suite específica
mvn test -Dtest=RegraPorIdadeTest

# Método específico
mvn test -Dtest=RegraPorIdadeTest#given_idade18_when_aplicar_then_permiteSemAnuidade

# Com coverage
mvn clean test jacoco:report
mvn jacoco:report # gera em target/site/jacoco/

# Executar UI
open target/site/jacoco/index.html
```

### Estrutura de Teste

```java
@DisplayName("RegraPorIdade - Teste de idade")
class RegraPorIdadeTest {
    
    private RegraPorIdade regra;
    
    @BeforeEach
    void setUp() {
        regra = new RegraPorIdade();
    }
    
    @Test
    @DisplayName("Deve permitir apenas SEM_ANUIDADE para menor de 25")
    void given_idade22_when_aplicar_then_permiteSemAnuidade() {
        Cliente cliente = TestFactory.criarCliente(22);
        List<CartaoOferta> cartoes = TestFactory.todosOsCartoes();
        
        List<CartaoOferta> resultado = regra.aplicar(cliente, cartoes);
        
        assertThat(resultado)
            .hasSize(1)
            .extracting(CartaoOferta::getTipoCartao)
            .contains(TipoCartao.CARTAO_SEM_ANUIDADE);
    }
}
```

---

## 📊 Logging e Auditoria

### Por que Logging é Crítico?

Em instituições financeiras, logging oferece:

1. **🔍 Rastreabilidade Completa**: Cada solicitação deixa rastro auditável
2. **🎯 Compliance Regulatório**: Banco Central, INTERPOL, LGPD
3. **⚠️ Detecção de Fraude**: Padrões suspeitos em tempo real
4. **📈 Analytics**: Taxa de aprovação, cartões populares, épocas

### Arquitetura de Logging

```
┌─────────────────────────────────────┐
│  SLF4J (Simple Logging Facade)      │ ← API unificada
├─────────────────────────────────────┤
│  Logback (Implementação)            │ ← Motor
├──────────────┬──────────────────────┤
│ ConsoleApp   │ FileAppender         │ ← Outputs
│ (DEV)        │ logs/cartoes.log     │
│              │ (PROD + DEV)         │
└──────────────┴──────────────────────┘
```

### Exemplo Real de Logs

**Cliente aprovado:**
```log
2026-03-26 14:30:00.125 [http-nio-8080-exec-1] INFO  CartaoService - Iniciando processamento de solicitação para CPF: 12345678901
2026-03-26 14:30:00.235 [http-nio-8080-exec-1] DEBUG ValidacaoClienteService - Validando cliente: nome=João, uf=SP
2026-03-26 14:30:00.340 [http-nio-8080-exec-1] DEBUG ElegibilidadeService - Processando elegibilidade para CPF: 12345678901
2026-03-26 14:30:00.445 [http-nio-8080-exec-1] DEBUG RegraPorIdade - Cliente 28 anos: permite todos os cartões
2026-03-26 14:30:00.550 [http-nio-8080-exec-1] DEBUG RegraPorRenda - Cliente renda R$8000: passa em todos
2026-03-26 14:30:00.655 [http-nio-8080-exec-1] INFO  CartaoService - ✓ 2 cartões aprovados: SEM_ANUIDADE, DE_PARCEIROS
2026-03-26 14:30:00.760 [http-nio-8080-exec-1] DEBUG CartaoService - Gerando resposta ID: a1b2c3d4...
```

**Cliente rejeitado (menor de 18):**
```log
2026-03-26 14:31:00.125 [http-nio-8080-exec-1] INFO  CartaoService - Iniciando processamento para CPF: 99988877766
2026-03-26 14:31:00.235 [http-nio-8080-exec-1] DEBUG ValidacaoClienteService - Validando cliente: nome=Maria, uf=RJ
2026-03-26 14:31:00.340 [http-nio-8080-exec-1] WARN  ValidacaoClienteService - ⚠️ Cliente 17 anos REJEITADO (< 18)
2026-03-26 14:31:00.445 [http-nio-8080-exec-1] ERROR CartaoService - Lançando ClienteInvalidoException: CLIENTE_MENOR_DE_IDADE (422)
```

### Configuração (logback-spring.xml)

```xml
<springProfile name="dev">
    <root level="DEBUG"/>
    <logger name="br.com.desafio.cartoes" level="DEBUG"/>
</springProfile>

<springProfile name="prod">
    <root level="WARN"/>
    <logger name="br.com.desafio.cartoes" level="INFO"/>
</springProfile>

<!-- Arquivo com rotação -->
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/cartoes.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>logs/cartoes.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>10MB</maxFileSize>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
</appender>
```

---

## 🐳 Docker e Deployment

### Dockerfile Multi-Stage

```dockerfile
# Stage 1: Build (Maven + JDK)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests -P prod

# Stage 2: Runtime (apenas JRE + JAR)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# Health check com Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s \
  CMD wget -O- http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Otimizações:**
- ✅ Build com profile `prod` (inclui todas as dependências)
- ✅ Imagem final ~180MB (vs 500+MB with full JDK)
- ✅ Apenas runtime, sem ferramentas de build
- ✅ Alpine Linux para footprint mínimo
- ✅ Health check automático

### Docker Compose (Produção)

```yaml
services:
  cartoes-api:
    build:
      context: .
      dockerfile: Dockerfile
      # ⚠️ IMPORTANTE: Dockerfile faz build com -P prod automaticamente
    container_name: cartoes-api
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
    environment:
      # Profile ativado dentro do container
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xms256m -Xmx512m -XX:+UseG1GC"
    healthcheck:
      test: ["CMD", "wget", "-O-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
networks:
  cartoes-network:
    driver: bridge
volumes:
  logs:
    driver: local
```

**Fluxo:**
1. `mvn clean package -P prod` → Maven compila com dependências prod
2. `docker-compose up` → Docker build (copia JAR gerado)
3. Container inicia com `SPRING_PROFILES_ACTIVE=prod`
4. ✓ Apenas Docker rodando na porta 8080

### Comandos Úteis

```bash
# ========== DESENVOLVIMENTO ==========
# Executar com profile dev (padrão, sem Docker)
mvn spring-boot:run

# Explicitamente com -P dev (mesmo resultado)
mvn spring-boot:run -P dev

# Rodar testes
mvn test

# ========== PRODUÇÃO ==========
# Build com profile prod (inclui docker-compose dependency)
mvn clean package -P prod

# Subir container
docker-compose up -d

# Ver logs em tempo real
docker-compose logs -f cartoes-api

# Parar e remover
docker-compose down

# Acessar container (debug)
docker-compose exec cartoes-api bash

# Verificar saúde
curl http://localhost:8080/actuator/health
```

### Health Check Endpoints

```bash
# Health simples
curl http://localhost:8080/actuator/health
# Resposta: {"status":"UP"}

# Health detalhado (dev only)
curl http://localhost:8080/actuator/health/details
```

---

## ⚡ Performance e Escalabilidade

### Características de Escalabilidade

✅ **Stateless**: Sem sessão HTTP, pronto para múltiplas instâncias  
✅ **Load Balancer Ready**: Sem sticky sessions  
✅ **In-Memory DB**: O(1) em produção usa DB real (migrations automáticas)  
✅ **Connection Pooling**: Hikari CP padrão do Spring Boot  
✅ **Response Caching**: Cartões carregados uma vez  

### Benchmarks Esperados

| Métrica | Valor |
|---------|-------|
| Response time (p50) | 50-100ms |
| Response time (p99) | 200-300ms |
| Throughput @ 200KB RAM | 500+ RPS |
| CPU por request | <5% |
| Memory footprint | ~256MB heap |

### Deployment Escalado

```bash
# Kubernetes exemplo (apenas ilustração)
kubectl scale deployment cartoes-api --replicas=3
kubectl autoscale deployment cartoes-api --min=1 --max=5
```

---

## 🔧 Troubleshooting

### Problema: "Port 8080 already in use" (Dev + Prod rodando juntos)

⚠️ **Novo problema com Profiles:**

Se você esqueceu de parar o `mvn spring-boot:run` e depois subiu `docker-compose up`, ambos tentarão usar 8080:

```bash
# SOLUÇÃO 1: Parar Maven antes de subir Docker
# Terminal 1: Maven rodando
mvn spring-boot:run
# Ctrl+C para parar

# Terminal 2: Subir Docker
docker-compose up -d
```

```bash
# SOLUÇÃO 2: Verificar quem está usando porta 8080
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>
```

```bash
# SOLUÇÃO 3: Mudar porta do Maven para desenvolvimento
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

**Lembrete:** Com Profiles, você **NÃO PRECISA** mais usar `SPRING_DOCKER_COMPOSE_ENABLED=false`, o profile `dev` já trata disso!

### Problema: "H2 Console não abre" (Desenvolvimento)

✅ H2 console **apenas disponível em profile `dev`** (via `application-dev.yml`)

```bash
# Verificar que você está em DEV
mvn spring-boot:run  # Ativado por padrão
```

**Acessar em:** `http://localhost:8080/h2-console`

| Campo | Valor |
|-------|-------|
| **JDBC URL** | `jdbc:h2:mem:cartoes` |
| **Usuario** | `sa` |
| **Senha** | (vazia) |

❌ **Em PROD** não terá H2 (usa banco externo via compose.yaml)

### Problema: "Test falha com ClassNotFoundException"

Certifique-se de rodar `mvn clean compile` antes de `mvn test`

```bash
mvn clean compile test
```

### Problema: "Docker build falha"

Certificar que `.dockerignore` exclui `target/` e `node_modules/`:

```dockerfile
# .dockerignore
target/
.git
node_modules
*.log
```

Se ainda falhar:
```bash
docker build --no-cache -t cartoes-api:latest .
```

### Problema: "POST /cartoes retorna 500"

1. Verificar logs: `docker-compose logs cartoes-api`
2. Validar JSON: `curl -v` mostra detalhes
3. Certificar que BD iniciou: Aguarde 2-3s após `docker-compose up`

---

## 📋 Referência de Erros

Todos os erros seguem **RFC 9457 (Problem Details)**:

```json
{
  "type": "about:blank",
  "title": "Error Type",
  "status": HTTP_STATUS,
  "detail": "Mensagem descritiva",
  "codigo_erro": "ERROR_CODE"
}
```

### Códigos de Erro

| Código | HTTP | Mensagem | Causa |
|--------|------|----------|-------|
| `CPF_INVALIDO` | 400 | CPF deve conter 11 dígitos | Formato CPF |
| `UF_INVALIDA` | 400 | UF inválida ou minúscula | Estado não existe |
| `CLIENTE_MENOR_DE_IDADE` | 422 | Cliente < 18 anos | Validação idade |
| `RENDA_INVALIDA` | 400 | Renda deve ser > 0 | Renda <= 0 |
| `ERRO_INTERNO` | 500 | Erro inesperado | Exception não tratada |

---

## 📊 Logging - Detalhes Completos

### Uso em Código

```java
package br.com.desafio.cartoes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CartaoService {
    
    public SolicitacaoResponseDTO solicitar(ClienteRequestDTO dto) {
        // INFO: eventos importantes
        log.info("Iniciando processamento para CPF: {}", dto.getCpf());
        
        // DEBUG: detalhes técnicos (dev only)
        log.debug("Dados completos: {}", dto);
        
        try {
            // ... lógica ...
        } catch (Exception e) {
            // ERROR: algo crítico falhou
            log.error("Erro ao processar solicitação para CPF: {}", dto.getCpf(), e);
            throw e;
        }
    }
}
```

---

## 📚 Estrutura do Projeto Completa

```
cartoes/
│
├── src/main/java/br/com/desafio/cartoes/
│   │
│   ├── CartoesApplication.java              # Entry point
│   │
│   ├── config/
│   │   └── DadosIniciais.java               # Carrega 3 cartões no startup
│   │
│   ├── controller/
│   │   └── CartoesController.java           # REST endpoint POST /cartoes
│   │
│   ├── service/
│   │   ├── CartaoService.java               # Orquestra fluxo
│   │   ├── ValidacaoClienteService.java     # Valida dados
│   │   └── ElegibilidadeService.java        # Aplica regras
│   │
│   ├── rule/                                # Strategy Pattern
│   │   ├── ElegibilidadeRule.java           # Interface
│   │   ├── RegraPorIdade.java               # Age rule
│   │   ├── RegraPorRenda.java               # Income rule
│   │   └── RegraPorUF.java                  # State rule
│   │
│   ├── domain/
│   │   ├── dto/
│   │   │   ├── ClienteRequestDTO.java       # Request
│   │   │   └── SolicitacaoResponseDTO.java  # Response
│   │   ├── entity/
│   │   │   └── CartaoOferta.java            # JPA entity
│   │   ├── enums/
│   │   │   └── TipoCartao.java              # Card types enum
│   │   └── model/
│   │       └── Cliente.java                 # Domain model
│   │
│   ├── exception/
│   │   ├── ClienteInvalidoException.java    # Custom exception
│   │   └── GlobalExceptionHandler.java      # RFC 9457 error handler
│   │
│   ├── repository/
│   │   └── CartaoRepository.java            # Spring Data JPA
│   │
│   └── util/
│       └── ClienteValidator.java            # Validation utilities
│
├── src/main/resources/
│   ├── application.yml                      # Spring config
│   └── logback-spring.xml                   # Logging config
│
├── src/test/java/br/com/desafio/cartoes/
│   ├── CartoesApplicationTests.java         # Integration test
│   │
│   ├── controller/
│   │   └── CartoesControllerTest.java       # REST endpoint testing
│   │
│   ├── rule/
│   │   ├── RegraPorIdadeTest.java
│   │   ├── RegraPorRendaTest.java
│   │   └── RegraPorUFTest.java              # Rule testing
│   │
│   ├── service/
│   │   ├── CartaoServiceTest.java
│   │   ├── ElegibilidadeServiceTest.java
│   │   └── ValidacaoClienteServiceTest.java # Service testing
│   │
│   ├── support/
│   │   └── TestFactory.java                 # Test fixtures & builders
│   │
│   └── util/
│       └── ClienteValidatorTest.java        # Utility testing
│
├── Dockerfile                                # Multi-stage build
├── compose.yaml                              # Docker Compose
├── .dockerignore                             # Build optimization
├── pom.xml                                   # Maven dependencies
└── README.md                                 # Este arquivo
```

---

## 🎓 Decisões de Design & Trade-offs

### 1. Strategy Pattern para Regras

**Decisão:** Usar interface `ElegibilidadeRule` com múltiplas implementações.

**Alternativa Rejeitada:** `if/else` gigante ou `switch` único.

**Por quê?**
```
✓ Fácil adicionar nova regra (veja seção Extensibilidade)
✓ Cada regra é testável isoladamente
✓ Aberto para extensão, fechado para modificação (Open/Closed Principle)
✓ Code review mais fácil (regras isoladas)
✗ Ligeiramente mais overhead (10-20 nanosegundos por classe)
```

### 2. H2 In-Memory para Banco de Dados

**Decisão:** H2 database em memória (`ddl-auto: create-drop`).

**Alternativa Rejeitada:** PostgreSQL ou MySQL persistente.

**Por quê?**
```
✓ Dev rápido (sem setup de banco externo)
✓ Testes isolados (cada execução limpa dados)
✓ Zero overhead de rede
✗ Dados perdidos ao reiniciar (intencional para dev)
→ Produção: Trocar apenas `application-prod.yml`
```

### 3. Validação em Camadas

**Decisão:** Bean Validation (@NotBlank) + ValidacaoClienteService custom.

**Alternativa Rejeitada:** Apenas Bean Validation.

**Por quê?**
```
✓ Bean Validation: Formato (CPF tem 11 dígitos)
✓ Custom Validator: Lógica de negócio (idade >= 18)
✓ Dois níveis = defesa em profundidade
✗ Ligeiramente mais verboso (~20 linhas extra)
```

### 4. RFC 9457 para Erros

**Decisão:** `GlobalExceptionHandler` retorna Problem Details.

**Alternativa Rejeitada:** JSON customizado.

**Por quê?**
```
✓ Padrão REST moderno (RFC 9457)
✓ Frontend pode parsear consistentemente
✓ Ferramentas conhecem o formato
✗ Menos liberdade para customização
```

---

## 🔗 Fluxo Completo (End-to-End)

```
CLIENT                  API                   SERVICE                  DB
  │                      │                       │                      │
  ├─ POST /cartoes ─────→│                       │                      │
  │  (ClienteRequestDTO) │                       │                      │
  │                      ├─ Validar estrutura    │                      │
  │                      │  (Bean Validation)    │                      │
  │                      │                       │                      │
  │                      ├─ ValidarCliente ─────→│ (age, CPF, UF)       │
  │                      │                       │                      │
  │                      ├─ Buscar cartões ─────────────────────────────→│
  │                      │                       │  (SELECT * FROM)     │
  │                      │←────────────────────────────────────── [3]   │
  │                      │                       │                      │
  │                      ├─ Aplicar regras:      │                      │
  │                      │  RegraPorIdade        │                      │
  │                      │  RegraPorRenda        │                      │
  │                      │  RegraPorUF           │                      │
  │                      │  → [cartões filtrados]│                      │
  │                      │                       │                      │
  │←─ 200 + cartões ─────│                       │                      │
  │  OU 204 (vazio)      │                       │                      │
  │  OU 400/422 (erro)   │                       │                      │
```

---

## 📝 Notas sobre Commits & Versionamento

**Versão Atual:** 1.1 (Commit 19 - Maven Profiles)  
**Status:** ✅ Logging + Docker + Testes + Maven Profiles implementados  

**Mudanças v1.1:**
- ✅ Implementado Maven Profiles (dev padrão, prod com Docker)
- ✅ Removida dependência docker-compose do escopo comum
- ✅ Criados `application-dev.yml` e `application-prod.yml`
- ✅ Resolvido conflito de porta 8080 (Maven vs Docker)

**Próximas Melhorias:**
- [ ] CI/CD Pipeline (GitHub Actions)
- [ ] Autoscaling com Kubernetes
- [ ] Criptografia de dados em repouso
- [ ] API versioning (v2 com novos cartões)

---

## 📖 Links Úteis

- **OpenAPI/Swagger**: http://localhost:8080/swagger-ui.html
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **RFC 9457**: https://tools.ietf.org/html/rfc9457 (Problem Details)
- **H2 Database**: https://www.h2database.com/
- **Logback Docs**: https://logback.qos.ch/

---

**Contribuições**: Para reportar bugs ou sugerir features, abra uma issue.  
**Licença**: MIT  
**Autor**: Desenvolvido como desafio técnico Itaú
