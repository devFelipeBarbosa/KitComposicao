package br.com.evolution.kitcomposicao.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegraEstoquePedidoTest {

    private RegraEstoquePedido regra(String atualEst, boolean valEst, BigDecimal atualEstMp) {
        return new RegraEstoquePedido(BigDecimal.ONE, atualEst, valEst, "N", atualEstMp);
    }

    @Test
    void servicoNuncaValidaEstoque() {
        assertFalse(regra("B", true, null).deveValidarEstoque("S"));
    }

    @Test
    void topQueBaixaEstoqueValida() {
        assertTrue(regra("B", false, null).deveValidarEstoque("R"));
    }

    @Test
    void topDeReservaValidaSomenteComValEstLigado() {
        assertTrue(regra("R", true, null).deveValidarEstoque("R"));
        assertFalse(regra("R", false, null).deveValidarEstoque("R"));
    }

    @Test
    void topSemAtualizacaoNaoValida() {
        assertFalse(regra("N", true, null).deveValidarEstoque("R"));
    }

    @Test
    void materiaPrimaSemReservaUsaAtualEstMp() {
        assertTrue(regra("N", false, BigDecimal.valueOf(-1)).deveValidarEstoque("M"));
        assertFalse(regra("N", false, BigDecimal.ZERO).deveValidarEstoque("M"));
    }
}
