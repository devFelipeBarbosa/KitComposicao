package br.com.evolution.kitcomposicao.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composição de Produto de um PA: regra de negócio pura, sem dependência
 * de Sankhya/JAPE.
 *
 * Regra do escopo: QTDNEG de cada MP = quantidade digitada do PA
 * multiplicada pela quantidade da MP na composição. Com a opção
 * "Listar PA?", o próprio PA também entra como item com a quantidade digitada.
 */
public final class ComposicaoPA {

    private final BigDecimal codProdPA;
    private final List<ItemComposicao> materiasPrimas;

    public ComposicaoPA(BigDecimal codProdPA, List<ItemComposicao> materiasPrimas) {
        if (codProdPA == null) {
            throw new IllegalArgumentException("Código do PA não pode ser nulo.");
        }
        if (materiasPrimas == null || materiasPrimas.isEmpty()) {
            throw new IllegalArgumentException("Composição sem matérias-primas cadastradas.");
        }
        this.codProdPA = codProdPA;
        this.materiasPrimas = Collections.unmodifiableList(new ArrayList<ItemComposicao>(materiasPrimas));
    }

    public List<ItemPedido> gerarItensPedido(BigDecimal qtdDigitada, boolean listarPA) {
        if (qtdDigitada == null || qtdDigitada.signum() <= 0) {
            throw new IllegalArgumentException("Quantidade digitada deve ser maior que zero.");
        }

        List<ItemPedido> itens = new ArrayList<ItemPedido>();

        if (listarPA) {
            itens.add(new ItemPedido(codProdPA, qtdDigitada));
        }

        for (ItemComposicao mp : materiasPrimas) {
            BigDecimal qtdNeg = qtdDigitada.multiply(mp.getQtdComposicao());
            itens.add(new ItemPedido(mp.getCodProdMP(), qtdNeg));
        }

        return itens;
    }

    public BigDecimal getCodProdPA() {
        return codProdPA;
    }
}
