package br.com.evolution.kitcomposicao.actions;

import br.com.evolution.kitcomposicao.domain.ResultadoInclusao;
import br.com.evolution.kitcomposicao.service.PreencherItensService;
import br.com.evolution.kitcomposicao.util.ParamUtil;
import br.com.evolution.kitcomposicao.util.RepositorioArquivos;
import br.com.evolution.kitcomposicao.util.TLogCatcher;
import br.com.evolution.kitcomposicao.util.TLogConfiguration;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;

/**
 * Botão de Ação (TGFITE/ItemNota — Centrais de Pedido).
 *
 * Recebe do formulário: CODPRODPA, CODLOCALORIG, QTDNEG e LISTAPA.
 * Insere na aba de Itens as MPs da Composição de Produto do PA,
 * multiplicando a quantidade digitada pela quantidade de cada MP.
 */
public class PreencherItensPorComposicaoPA implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        TLogConfiguration.setFileName("PreencherItensPorComposicaoPA");
        TLogConfiguration.setPath(RepositorioArquivos.pastaDeLog());

        try {
            BigDecimal nunota = obterNunota(ctx);

            BigDecimal codProdPA    = ParamUtil.getBigDecimalObrigatorio(ctx, "CODPRODPA", "Produto Acabado");
            BigDecimal codLocalOrig = ParamUtil.getBigDecimalObrigatorio(ctx, "CODLOCALORIG", "Local Origem");
            BigDecimal qtdDigitada  = ParamUtil.getBigDecimalObrigatorio(ctx, "QTDNEG", "Quantidade");
            boolean listarPA        = ParamUtil.getBooleanOpcional(ctx, "LISTAPA");
            BigDecimal idProc       = ParamUtil.getBigDecimalObrigatorio(ctx, "IDPROC", "Processo Produtivo");

            if (qtdDigitada.signum() <= 0) {
                ctx.mostraErro("Atenção! A Quantidade deve ser maior que zero.");
                return;
            }

            ResultadoInclusao resultado = new PreencherItensService()
                .executar(ctx, nunota, codProdPA, codLocalOrig, qtdDigitada, listarPA, idProc);

            TLogCatcher.logInfo("NUNOTA=" + nunota + " PA=" + codProdPA
                + " QTD=" + qtdDigitada + " LISTAPA=" + listarPA + " IDPROC=" + idProc
                + " -> " + resultado.getIncluidos() + " item(ns) inserido(s), "
                + resultado.getPulados() + " pulado(s) por estoque.");

            ctx.setMensagemRetorno(resultado.mensagemParaUsuario());

        } catch (Exception e) {
            if (ePerguntaPendenteDaTela(e)) {
                throw e;
            }
            TLogCatcher.logError("Falha ao preencher itens por Composição de PA.", e);
            throw e;
        } finally {
            TLogConfiguration.clear();
        }
    }

    /**
     * confirmarSimNao lança IllegalStateException com payload __TITULO__/__TEXTO__
     * na primeira execução (mecanismo da plataforma para exibir o diálogo e
     * reexecutar a ação com a resposta). Não é falha — não deve ir para o log.
     */
    private boolean ePerguntaPendenteDaTela(Exception e) {
        return e instanceof IllegalStateException
            && e.getMessage() != null
            && e.getMessage().contains("__TITULO__");
    }

    private BigDecimal obterNunota(ContextoAcao ctx) throws Exception {
        Registro linhaPai = ctx.getLinhaPai();
        Object nunota = (linhaPai != null) ? linhaPai.getCampo("NUNOTA") : null;

        if (nunota == null) {
            throw new Exception("Não foi possível identificar o pedido (NUNOTA). "
                + "Salve o cabeçalho do pedido antes de executar a ação.");
        }
        return new BigDecimal(nunota.toString());
    }
}
