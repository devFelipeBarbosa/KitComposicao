package br.com.evolution.kitcomposicao.domain;

import java.math.BigDecimal;

/**
 * Item pronto para inserção na aba de Itens da Central de Pedidos (TGFITE).
 */
public final class ItemPedido {

    private final BigDecimal codProd;
    private final BigDecimal qtdNeg;

    public ItemPedido(BigDecimal codProd, BigDecimal qtdNeg) {
        this.codProd = codProd;
        this.qtdNeg = qtdNeg;
    }

    public BigDecimal getCodProd() {
        return codProd;
    }

    public BigDecimal getQtdNeg() {
        return qtdNeg;
    }
}
