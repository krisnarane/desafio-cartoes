package br.com.desafio.cartoes.domain.enums;

public enum TipoErro {
    VALIDACAO_ENTRADA("Validação de entrada falhou"),
    REGRA_NEGOCIO("Regra de negócio violada"),
    SERVICO_INDISPONIVEL("Serviço indisponível"),
    ERRO_INTERNO("Erro interno do servidor"),
    RECURSO_NAO_ENCONTRADO("Recurso não encontrado");

    private final String descricao;

    TipoErro(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
