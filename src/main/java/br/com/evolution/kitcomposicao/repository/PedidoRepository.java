package br.com.evolution.kitcomposicao.repository;

import br.com.evolution.kitcomposicao.domain.RegraEstoquePedido;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;

import java.math.BigDecimal;

/**
 * Consulta o cabeçalho do pedido (TGFCAB) com a TOP na versão vinculada
 * ao pedido (TGFTOP por CODTIPOPER + DHALTER = DHTIPOPER).
 */
public final class PedidoRepository {

    private PedidoRepository() {}

    public static RegraEstoquePedido buscarRegraEstoque(QueryExecutor q, BigDecimal nunota) throws Exception {
        try {
            q.setParam("NUNOTA", nunota);
            q.nativeSelect(
                "SELECT CAB.CODEMP, TOP.ATUALEST, TOP.VALEST, TOP.STATUSBAIXAEST, TOP.ATUALESTMP"
              + "  FROM TGFCAB CAB"
              + " INNER JOIN TGFTOP TOP ON TOP.CODTIPOPER = CAB.CODTIPOPER"
              + "                      AND TOP.DHALTER = CAB.DHTIPOPER"
              + " WHERE CAB.NUNOTA = {NUNOTA}");

            if (!q.next()) {
                throw new Exception("Pedido " + nunota + " não encontrado (TGFCAB).");
            }
            return new RegraEstoquePedido(
                q.getBigDecimal("CODEMP"),
                q.getString("ATUALEST"),
                "S".equals(q.getString("VALEST")),
                q.getString("STATUSBAIXAEST"),
                q.getBigDecimal("ATUALESTMP"));
        } finally {
            q.close();
        }
    }
}
