package br.com.evolution.kitcomposicao.domain;

import java.math.BigDecimal;

/**
 * Situação do Processo Produtivo (TPRPRC) escolhido no formulário, frente ao
 * PA informado. Regras de uso da busca por composição:
 *
 * 0. processo precisa estar ativo;
 * 1. o PA precisa ter MPs vinculadas ao processo escolhido;
 * 2. apenas processos com UMA atividade configurada são permitidos.
 */
public final class ProcessoProdutivo {

    private final BigDecimal idProc;
    private final boolean ativo;
    private final int qtdAtividades;
    private final int qtdMpsDoPA;

    public ProcessoProdutivo(BigDecimal idProc, boolean ativo, int qtdAtividades, int qtdMpsDoPA) {
        this.idProc = idProc;
        this.ativo = ativo;
        this.qtdAtividades = qtdAtividades;
        this.qtdMpsDoPA = qtdMpsDoPA;
    }

    public void validarParaBuscaDeComposicao() throws Exception {
        if (!ativo) {
            throw new Exception("O processo produtivo " + idProc + " não está ativo.");
        }
        if (qtdMpsDoPA == 0) {
            throw new Exception("O item (PA) não possui vínculo com o processo produtivo escolhido ("
                + idProc + ").");
        }
        if (qtdAtividades > 1) {
            throw new Exception("O processo produtivo " + idProc + " possui mais de uma atividade. "
                + "Esta busca permite apenas processos com uma atividade configurada "
                + "e que contenham MPs vinculadas.");
        }
    }
}
