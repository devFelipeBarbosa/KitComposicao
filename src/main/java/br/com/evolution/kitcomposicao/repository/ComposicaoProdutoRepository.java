package br.com.evolution.kitcomposicao.repository;

import br.com.evolution.kitcomposicao.domain.ItemComposicao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à Composição de Produto do módulo de Produção
 * (TPRLMP lista de MPs / TPRATV atividades / TPRPRC processo).
 *
 * Considera sempre a versão mais recente do processo (TPRPRC.VERSAO).
 */
public final class ComposicaoProdutoRepository {

    // Coluna confirmada no Dicionário de Dados (07/07/2026).
    private static final String COLUNA_QTD_MP = "QTDMISTURA";

    private ComposicaoProdutoRepository() {}

    /**
     * MPs do PA restritas ao processo escolhido no formulário (IDPROC).
     * ROW_NUMBER por VERSAO garante a composição/versão mais recente.
     */
    public static List<ItemComposicao> buscarMateriasPrimas(QueryExecutor q,
                                                            BigDecimal codProdPA,
                                                            BigDecimal idProc) throws Exception {
        List<ItemComposicao> materiasPrimas = new ArrayList<ItemComposicao>();
        try {
            q.setParam("CODPRODPA", codProdPA);
            q.setParam("IDPROC", idProc);
            q.nativeSelect(
                "SELECT CODPRODMP, QTDMP" +
                " FROM (" +
                "   SELECT MP.CODPRODMP," +
                "          MP." + COLUNA_QTD_MP + " AS QTDMP," +
                "          ROW_NUMBER() OVER (PARTITION BY MP.CODPRODMP ORDER BY PRC.VERSAO DESC) AS RN" +
                "   FROM TPRLMP MP" +
                "   INNER JOIN TPRATV ATV ON ATV.IDEFX = MP.IDEFX" +
                "   INNER JOIN TPRPRC PRC ON PRC.IDPROC = ATV.IDPROC" +
                "   WHERE MP.CODPRODPA = {CODPRODPA}" +
                "     AND PRC.IDPROC = {IDPROC}" +
                " )" +
                " WHERE RN = 1"
            );

            while (q.next()) {
                materiasPrimas.add(new ItemComposicao(
                    q.getBigDecimal("CODPRODMP"),
                    q.getBigDecimal("QTDMP")
                ));
            }
            return materiasPrimas;
        } finally {
            q.close();
        }
    }
}
