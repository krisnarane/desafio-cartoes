# 💳 Cartões - API de Elegibilidade de Cartões de Crédito

Sistema de elegibilidade para cartões de crédito com validações complexas, logging completo e containerização Docker.

## 📋 Resumo Técnico

- **Linguagem**: Java 17
- **Framework**: Spring Boot 4.0.4
- **Database**: H2 (em memória)
- **API**: REST com OpenAPI/Swagger
- **Padrões**: Strategy Pattern, RFC 9457 (Problem Details)
- **Logging**: SLF4J + Logback
- **Docker**: Multi-stage build

---

## 🚀 Quick Start

### Sem Docker (Desenvolvimento)

```bash
# Compilar
mvn clean compile

# Executar aplicação
mvn spring-boot:run

# Rodar testes
mvn test
```

App está em: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`
API Docs: `http://localhost:8080/v3/api-docs`

### Com Docker (Produção)

```bash
# Build da imagem
docker build -t cartoes-api:latest .

# Executar container
docker run -p 8080:8080 -v logs:/app/logs cartoes-api:latest

# Ou com Docker Compose
docker-compose up -d
```

---

## 📊 Logging - Explicação Detalhada

### Por que Logging é Crítico?

Em um sistema de cartões de crédito, logging é **essencial** para:

1. **🔍 Rastreabilidade**: Rastrear cada solicitação desde recepção até decisão
   ```
   [2026-03-24 21:15:30.125] Iniciando processamento de solicitação para CPF: 12345678901
   [2026-03-24 21:15:30.235] Processando elegibilidade do cliente: 12345678901
   [2026-03-24 21:15:30.340] Aplicando regra: RegraPorRenda - RJ, renda R$6000
   [2026-03-24 21:15:30.445] Cliente elegível para: CARTAO_SEM_ANUIDADE, CARTAO_DE_PARCEIROS
   ```

2. **🎯 Auditoria Regulatória**: Itaú precisa comprovar decisões para fiscalização
   - Quem solicitou
   - Quando solicitou
   - Qual foi a decisão
   - Por qual motivo foi rejeitado

3. **⚠️ Detecção de Fraude**: Identificar padrões suspeitos
   - Múltiplas solicitações do mesmo CPF em curto espaço
   - Tentativas de validação com CPF inválido
   - Variação anormal de renda

4. **📈 Analytics & Business Intelligence**
   - Taxa de aprovação por estado
   - Cartão mais solicitado
   - Horários de pico

### Arquitetura de Logging

```
┌─ ConsoleAppender (DEBUG em desenvolvimento)
├─ FileAppender (logs/cartoes.log) → Todos os eventos
├─ BusinessAppender (logs/business.log) → Eventos de negócio
└─ Profiles: dev (DEBUG) | prod (WARN)
```

### Exemplo de Log em Ação

**Cenário**: Cliente João Silva solicita cartão

```log
2026-03-24 21:15:30.125 [http-nio-8080-exec-1] INFO  CartaoService - Iniciando processamento de solicitação para CPF: 12345678901
2026-03-24 21:15:30.235 [http-nio-8080-exec-1] DEBUG ValidacaoClienteService - Iniciando validação do cliente: 12345678901
2026-03-24 21:15:30.340 [http-nio-8080-exec-1] DEBUG ElegibilidadeService - Processando elegibilidade do cliente: 12345678901
2026-03-24 21:15:30.445 [http-nio-8080-exec-1] INFO  CartaoService - Cartões elegíveis encontrados: 2 cartões
2026-03-24 21:15:30.550 [http-nio-8080-exec-1] DEBUG CartaoService - Gerando resposta para solicitação ID: a1b2c3d4-e5f6-47g8-h9i0-j1k2l3m4n5o6
```

**Cenário**: Cliente rejeitado por idade < 18 anos

```log
2026-03-24 21:15:30.125 [http-nio-8080-exec-1] INFO  CartaoService - Iniciando processamento de solicitação para CPF: 99988877766
2026-03-24 21:15:30.235 [http-nio-8080-exec-1] DEBUG ValidacaoClienteService - Iniciando validação do cliente: 99988877766
2026-03-24 21:15:30.340 [http-nio-8080-exec-1] WARN  ValidacaoClienteService - Cliente menor de 18 anos rejeitado: CPF 99988877766, idade 17
2026-03-24 21:15:30.445 [http-nio-8080-exec-1] ERROR CartaoService - Erro ao processar solicitação: ClienteInvalidoException - Cliente menor de 18 não pode ser elegível
```

### Configuração de Logging

Arquivo: `logback-spring.xml`

```xml
<!-- Development: DEBUG para cartões, WARN para frameworks -->
<springProfile name="dev">
    <root level="DEBUG"/>
</springProfile>

<!-- Production: WARN (menos ruído, melhor performance) -->
<springProfile name="prod">
    <root level="WARN"/>
</springProfile>
```

Uso em código:

```java
@Service
@Slf4j
public class CartaoService {
    public SolicitacaoResponseDTO solicitar(ClienteRequestDTO dto) {
        log.info("Iniciando processamento para CPF: {}", dto.getCpf());
        log.debug("Dados: {}", dto);
        log.warn("Problema identificado: {}", warning);
        log.error("Erro crítico: {}", error);
    }
}
```

---

## 🐳 Docker - Configuração

### Dockerfile Multi-Stage

```dockerfile
# Stage 1: Build (maven + JDK)
FROM maven:3.9-eclipse-temurin-17 AS builder
RUN mvn clean package -DskipTests

# Stage 2: Runtime (apenas JRE + JAR compilado)
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /build/target/*.jar app.jar
HEALTHCHECK --interval=30s --timeout=10s
EXPOSE 8080
```

**Benefícios**:
- ✅ Imagem final ~180MB (sem source code, sem Maven)
- ✅ Segurança: Sem ferramentas de build em produção
- ✅ Health check automático (Actuator)

### Docker Compose

```yaml
services:
  cartoes-api:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs  # Logs persistem no host
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xms256m -Xmx512m"
```

### Comandos Úteis

```bash
# Build e subir
docker-compose up --build

# Ver logs em tempo real
docker-compose logs -f cartoes-api

# Parar
docker-compose down

# View logs salvos no host
tail -f logs/business.log
```

---

## 📚 Estrutura do Projeto

```
cartoes/
├── src/main/java/br/com/desafio/cartoes/
│   ├── controller/              # RestControllers (endpoints)
│   ├── service/                 # Lógica de negócio (logging aqui)
│   ├── rule/                    # Strategy Pattern (elegibilidade)
│   ├── domain/                  # Entities, DTOs, Models
│   ├── exception/               # Exceções customizadas
│   └── util/                    # Validadores, helpers
├── src/main/resources/
│   ├── application.yml          # Config do Spring
│   └── logback-spring.xml       # Config de logging
├── Dockerfile                   # Multi-stage build
├── compose.yaml                 # Docker Compose
└── .dockerignore               # Otimização de build
```

---

## 🔐 Regras de Elegibilidade (Logging)

Cada regra registra sua decisão:

```java
@Slf4j
public class RegraPorRenda implements ElegibilidadeRule {
    public List<CartaoOferta> aplicar(Cliente cliente, List<CartaoOferta> cartoes) {
        log.debug("Aplicando RegraPorRenda: CPF {}, renda R${}", 
                  cliente.getCpf(), cliente.getRendaMensal());
        // Filtra por renda mínima
        log.info("RegraPorRenda: {} cartões aprovados para renda R${}", 
                resultado.size(), cliente.getRendaMensal());
        return resultado;
    }
}
```

---

## 🧪 Testes (Próximo Passo)

```bash
# Unit tests para services
mvn test -Dtest=ElegibilidadeServiceTest

# Integration tests para controller
mvn test -Dtest=CartoesControllerIntegrationTest

# Coverage
mvn test jacoco:report
```

---

## 📞 Endpoints da API

### POST /cartoes - Solicitar Elegibilidade

**Request**:
```json
{
  "nome": "João Silva",
  "cpf": "12345678901",
  "idade": 28,
  "data_nascimento": "1996-05-15",
  "uf": "RJ",
  "renda_mensal": 6000,
  "email": "joao@email.com",
  "telefone_whatsapp": "21987654321"
}
```

**Response (200 OK)**:
```json
{
  "numero_solicitacao": "a1b2c3d4-e5f6-47g8",
  "data_solicitacao": "2026-03-24T21:15:30.123",
  "cliente": {
    "nome": "João Silva",
    "cpf": "12345678901"
  },
  "cartoes_ofertados": [
    {
      "tipo_cartao": "CARTAO_SEM_ANUIDADE",
      "valor_anuidade_mensal": 0,
      "valor_limite_disponivel": 5000,
      "status": "APROVADO"
    },
    {
      "tipo_cartao": "CARTAO_DE_PARCEIROS",
      "valor_anuidade_mensal": 29,
      "valor_limite_disponivel": 8000,
      "status": "APROVADO"
    }
  ]
}
```

**Response (204 No Content)**: Nenhum cartão elegível

**Response (422 Unprocessable Entity)**:
```json
{
  "type": "about:blank",
  "title": "Unprocessable Entity",
  "status": 422,
  "detail": "Cliente menor de 18 anos não pode ser elegível",
  "codigo_erro": "CLIENTE_MENOR_DE_IDADE"
}
```

---



---

**Versão**: 1.0 (Commit 18)  
**Status**: ✅ Logging + Docker implementados
