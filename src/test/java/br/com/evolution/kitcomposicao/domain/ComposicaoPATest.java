package br.com.evolution.kitcomposicao.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComposicaoPATest {

    private static final BigDecimal PA = new BigDecimal(100);
    private static final BigDecimal MP_A = new BigDecimal(200);
    private static final BigDecimal MP_B = new BigDecimal(300);

    private ComposicaoPA composicaoPadrao() {
        return new ComposicaoPA(PA, Arrays.asList(
            new ItemComposicao(MP_A, new BigDecimal("2")),
            new ItemComposicao(MP_B, new BigDecimal("0.5"))
        ));
    }

    @Test
    void multiplicaQuantidadeDigitadaPelaQuantidadeDaComposicao() {
        List<ItemPedido> itens = composicaoPadrao().gerarItensPedido(new BigDecimal("10"), false);

        assertEquals(2, itens.size());
        assertEquals(MP_A, itens.get(0).getCodProd());
        assertEquals(0, new BigDecimal("20").compareTo(itens.get(0).getQtdNeg()));
        assertEquals(MP_B, itens.get(1).getCodProd());
        assertEquals(0, new BigDecimal("5").compareTo(itens.get(1).getQtdNeg()));
    }

    @Test
    void listarPAIncluiOProprioPAComQuantidadeDigitada() {
        List<ItemPedido> itens = composicaoPadrao().gerarItensPedido(new BigDecimal("10"), true);

        assertEquals(3, itens.size());
        assertEquals(PA, itens.get(0).getCodProd());
        assertEquals(0, new BigDecimal("10").compareTo(itens.get(0).getQtdNeg()));
    }

    @Test
    void quantidadeZeroOuNegativaEhRejeitada() {
        ComposicaoPA composicao = composicaoPadrao();

        assertThrows(IllegalArgumentException.class,
            () -> composicao.gerarItensPedido(BigDecimal.ZERO, false));
        assertThrows(IllegalArgumentException.class,
            () -> composicao.gerarItensPedido(new BigDecimal("-1"), false));
    }

    @Test
    void composicaoSemMPsEhRejeitada() {
        assertThrows(IllegalArgumentException.class,
            () -> new ComposicaoPA(PA, Collections.<ItemComposicao>emptyList()));
    }
}
