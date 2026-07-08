package br.com.evolution.kitcomposicao.domain;

import java.math.BigDecimal;

/**
 * Matéria-prima (MP) de uma Composição de Produto, com a quantidade
 * cadastrada na composição do PA.
 */
public final class ItemComposicao {

    private final BigDecimal codProdMP;
    private final BigDecimal qtdComposicao;

    public ItemComposicao(BigDecimal codProdMP, BigDecimal qtdComposicao) {
        if (codProdMP == null) {
            throw new IllegalArgumentException("Código da matéria-prima não pode ser nulo.");
        }
        if (qtdComposicao == null) {
            throw new IllegalArgumentException("Quantidade da composição não pode ser nula.");
        }
        this.codProdMP = codProdMP;
        this.qtdComposicao = qtdComposicao;
    }

    public BigDecimal getCodProdMP() {
        return codProdMP;
    }

    public BigDecimal getQtdComposicao() {
        return qtdComposicao;
    }
}
