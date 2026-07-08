package br.com.evolution.kitcomposicao.domain;

import java.math.BigDecimal;

/**
 * Definições de estoque da TOP do pedido (TGFTOP, na versão do cabeçalho)
 * necessárias para decidir se um item precisa de validação de estoque —
 * espelho da regra nativa (EstoqueHelpper.getAtualizacaoEstoque +
 * KitIndependenteHelper.validaEstoqueComp, build 4.35b491).
 */
public final class RegraEstoquePedido {

    private final BigDecimal codEmp;
    private final String atualEst;
    private final boolean validaEstoqueTOP;
    private final String statusBaixaEst;
    private final BigDecimal atualEstMp;

    public RegraEstoquePedido(BigDecimal codEmp,
                              String atualEst,
                              boolean validaEstoqueTOP,
                              String statusBaixaEst,
                              BigDecimal atualEstMp) {
        this.codEmp = codEmp;
        this.atualEst = atualEst;
        this.validaEstoqueTOP = validaEstoqueTOP;
        this.statusBaixaEst = statusBaixaEst;
        this.atualEstMp = atualEstMp;
    }

    public BigDecimal getCodEmp() {
        return codEmp;
    }

    public String getStatusBaixaEst() {
        return statusBaixaEst;
    }

    public boolean eTopDeReserva() {
        return "R".equals(atualEst);
    }

    /**
     * Item de serviço nunca valida. Demais itens validam quando a TOP baixa
     * estoque (ATUALESTOQUE = -1) ou quando reserva com "Validar estoque"
     * ligado na TOP (VALEST = 'S').
     */
    public boolean deveValidarEstoque(String usoProd) {
        if ("S".equals(usoProd)) {
            return false;
        }
        int atualEstoque = calcularAtualEstoque(usoProd);
        return atualEstoque == -1 || (eTopDeReserva() && validaEstoqueTOP);
    }

    private int calcularAtualEstoque(String usoProd) {
        if ("M".equals(usoProd) && !eTopDeReserva()) {
            return (atualEstMp != null) ? atualEstMp.intValue() : 0;
        }
        if ("B".equals(atualEst)) {
            return -1;
        }
        if ("R".equals(atualEst) || "E".equals(atualEst)) {
            return 1;
        }
        return 0;
    }
}
