package br.com.desafio.cartoes.domain.enums;

public enum TipoCartao {
    CARTAO_SEM_ANUIDADE("Cartão de crédito sem anuidade", 3500.00),
    CARTAO_COM_CASHBACK("Cartão de crédito com cashback", 7500.00),
    CARTAO_DE_PARCEIROS("Cartão de crédito de parceiros", 5500.00);

    private final String descricao;
    private final double rendaMinima;

    TipoCartao(String descricao, double rendaMinima) {
        this.descricao = descricao;
        this.rendaMinima = rendaMinima;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getRendaMinima() {
        return rendaMinima;
    }
}
