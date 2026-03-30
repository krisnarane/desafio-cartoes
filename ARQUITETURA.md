# Arquitetura do Projeto

Documentacao visual do fluxo e estrutura da API de Cartoes.

---

## Diagrama de Sequencia - Fluxo Completo `POST /cartoes`

Este diagrama mostra o caminho completo de uma requisicao, desde a chegada no controller ate o retorno HTTP (200, 204, 400, 422, 500).

```plantuml
@startuml
title Fluxo Completo - POST /cartoes

actor Cliente as client
participant "CartoesController" as controller
participant "Spring Validation" as validation
participant "CartaoService" as service
participant "ValidacaoClienteService" as validacao
participant "ElegibilidadeService" as elegibilidade
participant "CartaoRepository" as repo
participant "RegraPorIdade" as regraIdade
participant "RegraPorRenda" as regraRenda
participant "RegraPorUF" as regraUF
participant "SolicitacaoRepository" as solRepo
database "H2 Database" as db

== 1. Request HTTP ==
client -> controller : POST /cartoes\n{ "cliente": { nome, cpf, idade,\ndata_nascimento, uf, renda_mensal,\nemail, telefone_whatsapp } }

== 2. Bean Validation (Jakarta) ==
controller -> validation : @Valid @RequestBody
alt Campos obrigatorios faltando / formato invalido
    validation --> controller : MethodArgumentNotValidException
    controller --> client : **400 Bad Request**\n{ codigo: "400",\nmensagem: "Validacao de entrada falhou",\ndetalhe_erro: { tipo_erro: "VALIDACAO_ENTRADA" } }
end

alt JSON malformado / body vazio
    validation --> controller : HttpMessageNotReadableException
    controller --> client : **400 Bad Request**\n{ codigo: "400",\nmensagem: "Corpo da requisicao invalido" }
end

== 3. Processamento no Service ==
controller -> service : solicitar(clienteDTO)

== 4. Validacao de Regras de Negocio ==
service -> validacao : validar(clienteDTO)
validacao -> validacao : validar CPF formato
validacao -> validacao : validar UF (27 estados)
validacao -> validacao : validar idade >= 18
validacao -> validacao : validar coerencia idade/data_nascimento

alt CPF invalido
    validacao --> service : ClienteInvalidoException(400)
    service --> controller : propaga excecao
    controller --> client : **400 Bad Request**\n{ tipo_erro: "REGRA_NEGOCIO" }
end

alt Menor de 18 anos
    validacao --> service : ClienteInvalidoException(422)
    service --> controller : propaga excecao
    controller --> client : **422 Unprocessable Entity**\n{ mensagem: "Cliente menor de 18 anos" }
end

alt UF invalida
    validacao --> service : ClienteInvalidoException(400)
    service --> controller : propaga excecao
    controller --> client : **400 Bad Request**\n{ tipo_erro: "REGRA_NEGOCIO" }
end

== 5. Conversao para Domain Model ==
service -> validacao : converterParaModelo(clienteDTO)
validacao --> service : Cliente (domain)

== 6. Processamento de Elegibilidade ==
service -> elegibilidade : processar(cliente)

elegibilidade -> repo : findByAtivoTrue()
repo -> db : SELECT * FROM cartao_oferta\nWHERE ativo = true
db --> repo : [3 cartoes]
repo --> elegibilidade : List<CartaoOferta>

== 7. Aplicacao Sequencial das Regras (Strategy Pattern) ==

note over elegibilidade : Regra 1: Filtro por Idade
elegibilidade -> regraIdade : aplicar(cliente, [3 cartoes])
alt idade > 18 AND idade < 25
    regraIdade --> elegibilidade : [CARTAO_SEM_ANUIDADE]
else idade 18 OU idade >= 25
    regraIdade --> elegibilidade : [3 cartoes] (sem filtro)
end

note over elegibilidade : Regra 2: Filtro por Renda
elegibilidade -> regraRenda : aplicar(cliente, cartoes)
note right of regraRenda
  renda >= 3500 -> SEM_ANUIDADE
  renda >= 5500 -> + PARCEIROS
  renda >= 7500 -> + CASHBACK
end note
regraRenda --> elegibilidade : cartoes filtrados

note over elegibilidade : Regra 3: Filtro por UF
elegibilidade -> regraUF : aplicar(cliente, cartoes)
alt UF == "SP"
    alt idade > 25 AND idade < 30
        regraUF --> elegibilidade : todos os cartoes
    else idade <= 25 OU idade >= 30
        regraUF --> elegibilidade : remove CARTAO_DE_PARCEIROS
    end
else UF != "SP"
    regraUF --> elegibilidade : todos os cartoes
end

elegibilidade --> service : ResultadoElegibilidade\n{ cartoesAprovados: [...] }

== 8. Montagem da Resposta ==
service -> service : converter CartaoOferta -> CartaoResponseDTO\n(CARTAO_SEM_ANUIDADE -> anuidade = 0.00)
service -> service : gerar UUID (numero_solicitacao)

== 9. Persistencia para Auditoria ==
service -> solRepo : save(Solicitacao)
solRepo -> db : INSERT INTO solicitacao\n(numero_solicitacao, cpf, data)
db --> solRepo : OK
solRepo --> service : Solicitacao salva

service --> controller : SolicitacaoResponseDTO

== 10. Retorno HTTP ==
alt cartoesOfertados NAO vazio
    controller --> client : **200 OK**\n{ numero_solicitacao, data_solicitacao,\ncliente, cartoes_ofertados }
else cartoesOfertados vazio
    controller --> client : **204 No Content**\n(sem corpo)
end

@enduml
```

---

## Diagrama de Classes - Arquitetura

Mostra a estrutura de pacotes, classes e seus relacionamentos.

```plantuml
@startuml
title Arquitetura de Classes - Cartoes API

package "controller" {
    class CartoesController {
        - cartaoService: CartaoService
        + solicitar(request): ResponseEntity
    }
}

package "service" {
    class CartaoService {
        - validacaoService: ValidacaoClienteService
        - elegibilidadeService: ElegibilidadeService
        - solicitacaoRepository: SolicitacaoRepository
        + solicitar(clienteDTO): SolicitacaoResponseDTO
        - converterParaResponse(cartao): CartaoResponseDTO
    }

    class ValidacaoClienteService {
        + validar(clienteDTO): void
        + converterParaModelo(dto): Cliente
    }

    class ElegibilidadeService {
        - cartaoRepository: CartaoRepository
        - regras: List<ElegibilidadeRule>
        + processar(cliente): ResultadoElegibilidade
    }
}

package "rule" {
    interface ElegibilidadeRule <<Strategy>> {
        + aplicar(cliente, cartoes): List<CartaoOferta>
    }

    class RegraPorIdade implements ElegibilidadeRule
    class RegraPorRenda implements ElegibilidadeRule
    class RegraPorUF implements ElegibilidadeRule
}

package "domain.dto" {
    class SolicitacaoRequestDTO {
        + cliente: ClienteRequestDTO
    }
    class ClienteRequestDTO {
        + nome: String
        + cpf: String
        + idade: Integer
        + dataNascimento: LocalDate
        + uf: String
        + rendaMensal: BigDecimal
        + email: String
        + telefoneWhatsapp: String
    }
    class SolicitacaoResponseDTO {
        + numeroSolicitacao: String
        + dataSolicitacao: LocalDateTime
        + cliente: ClienteRequestDTO
        + cartoesOfertados: List<CartaoResponseDTO>
    }
    class CartaoResponseDTO {
        + tipoCartao: String
        + valorAnuidadeMensal: BigDecimal
        + valorLimiteDisponivel: BigDecimal
        + status: String
    }
    class ErroResponseDTO {
        + codigo: String
        + mensagem: String
        + detalheErro: ErroDetalheDTO
    }
}

package "domain.entity" {
    class CartaoOferta <<Entity>> {
        + id: Long
        + tipoCartao: TipoCartao
        + rendaMinima: BigDecimal
        + valorAnuidadeMensal: BigDecimal
        + valorLimiteDisponivel: BigDecimal
        + ativo: Boolean
    }
    class Solicitacao <<Entity>> {
        + id: Long
        + numeroSolicitacao: String
        + cpfCliente: String
        + dataSolicitacao: LocalDateTime
    }
}

package "domain.model" {
    class Cliente {
        + cpf: String
        + uf: String
        + rendaMensal: BigDecimal
        + dataNascimento: LocalDate
        + calcularIdade(): int
    }
    class ResultadoElegibilidade {
        + cartoesAprovados: List<CartaoOferta>
        + comCartoes(cartoes): ResultadoElegibilidade
    }
}

package "exception" {
    class GlobalExceptionHandler <<ControllerAdvice>> {
        + handleValidationErrors(): 400
        + handleHttpMessageNotReadable(): 400
        + handleClienteInvalidoException(): 400|422
        + handleGeneralException(): 500
    }
    class ClienteInvalidoException {
        + codigoErro: String
        + statusCode: int
    }
}

CartoesController --> CartaoService
CartaoService --> ValidacaoClienteService
CartaoService --> ElegibilidadeService
ElegibilidadeService --> ElegibilidadeRule
ElegibilidadeService ..> CartaoOferta : carrega
CartaoService ..> Solicitacao : persiste
GlobalExceptionHandler ..> ClienteInvalidoException : trata

@enduml
```

---

## Como Visualizar os Diagramas

Os diagramas acima estao em formato [PlantUML](https://plantuml.com/). Para visualiza-los:

1. **Online**: Copie o codigo entre `@startuml` e `@enduml` e cole em [plantuml.com](https://www.plantuml.com/plantuml/uml)
2. **VS Code**: Instale a extensao "PlantUML" e use `Alt+D` para preview
3. **IntelliJ**: Instale o plugin "PlantUML Integration"

---

## Resumo dos Status HTTP

| Codigo | Quando | Quem Decide |
|--------|--------|-------------|
| **200 OK** | Cliente tem pelo menos 1 cartao aprovado | `CartoesController` |
| **204 No Content** | Nenhum cartao elegivel apos aplicar regras | `CartoesController` |
| **400 Bad Request** | JSON invalido, campos faltando, formato errado | `GlobalExceptionHandler` |
| **422 Unprocessable Entity** | Regra de negocio violada (ex: menor de 18) | `GlobalExceptionHandler` |
| **500 Internal Server Error** | Erro inesperado na aplicacao | `GlobalExceptionHandler` |
