package br.com.desafio.cartoes.domain.enums;

public enum StatusOferta {
    APROVADO("Cartão aprovado"),
    REJEITADO("Cartão rejeitado");

    private final String descricao;

    StatusOferta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
