package br.com.evolution.kitcomposicao.repository;

import br.com.sankhya.extensions.actionbutton.QueryExecutor;

import java.math.BigDecimal;

/**
 * Consultas ao cadastro de produtos (TGFPRO).
 */
public final class ProdutoRepository {

    private ProdutoRepository() {}

    /**
     * Retorna o "Usado como" (USOPROD) do produto — 'S' serviço, 'M' matéria-prima etc.
     * Lança exceção se o produto não existir no cadastro.
     */
    public static String buscarUsoProd(QueryExecutor q, BigDecimal codProd) throws Exception {
        try {
            q.setParam("CODPROD", codProd);
            q.nativeSelect("SELECT USOPROD FROM TGFPRO WHERE CODPROD = {CODPROD}");

            if (!q.next()) {
                throw new Exception("Produto " + codProd + " não encontrado no cadastro (TGFPRO).");
            }
            return q.getString("USOPROD");
        } finally {
            q.close();
        }
    }
}
