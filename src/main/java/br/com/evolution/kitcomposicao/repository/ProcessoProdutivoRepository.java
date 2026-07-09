package br.com.evolution.kitcomposicao.repository;

import br.com.evolution.kitcomposicao.domain.ProcessoProdutivo;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;

import java.math.BigDecimal;

/**
 * Consulta o Processo Produtivo (TPRPRC) com os dados necessários para as
 * validações da busca: ativo (TPRPRC.ATIVO, confirmado no dicionário),
 * quantidade de atividades (TPRATV) e MPs do PA vinculadas ao processo
 * (TPRLMP via TPRATV).
 */
public final class ProcessoProdutivoRepository {

    private ProcessoProdutivoRepository() {}

    public static ProcessoProdutivo buscar(QueryExecutor q,
                                           BigDecimal idProc,
                                           BigDecimal codProdPA) throws Exception {
        try {
            q.setParam("IDPROC", idProc);
            q.setParam("CODPRODPA", codProdPA);
            q.nativeSelect(
                "SELECT PRC.ATIVO,"
              + "       (SELECT COUNT(*) FROM TPRATV ATV"
              + "         WHERE ATV.IDPROC = PRC.IDPROC) AS QTDATIVIDADES,"
              + "       (SELECT COUNT(*) FROM TPRLMP MP"
              + "         INNER JOIN TPRATV ATV2 ON ATV2.IDEFX = MP.IDEFX"
              + "         WHERE ATV2.IDPROC = PRC.IDPROC"
              + "           AND MP.CODPRODPA = {CODPRODPA}) AS QTDMPSDOPA"
              + "  FROM TPRPRC PRC"
              + " WHERE PRC.IDPROC = {IDPROC}");

            if (!q.next()) {
                throw new Exception("Processo produtivo " + idProc + " não encontrado (TPRPRC).");
            }
            return new ProcessoProdutivo(
                idProc,
                "S".equals(q.getString("ATIVO")),
                q.getInt("QTDATIVIDADES"),
                q.getInt("QTDMPSDOPA"));
        } finally {
            q.close();
        }
    }
}
