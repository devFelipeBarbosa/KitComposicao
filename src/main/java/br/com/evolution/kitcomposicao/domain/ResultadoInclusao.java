package br.com.evolution.kitcomposicao.domain;

/**
 * Resultado da inclusão de itens: quantos entraram e quantos foram pulados
 * pelo usuário ao recusar a efetivação sem estoque.
 */
public final class ResultadoInclusao {

    private final int incluidos;
    private final int pulados;

    public ResultadoInclusao(int incluidos, int pulados) {
        this.incluidos = incluidos;
        this.pulados = pulados;
    }

    public int getIncluidos() {
        return incluidos;
    }

    public int getPulados() {
        return pulados;
    }

    public String mensagemParaUsuario() {
        StringBuilder msg = new StringBuilder("Itens inseridos com sucesso! Total: ")
            .append(incluidos).append(" item(ns).");
        if (pulados > 0) {
            msg.append(" Não incluído(s) por falta de estoque: ").append(pulados).append(" item(ns).");
        }
        return msg.toString();
    }
}
