package br.com.evolution.kitcomposicao.repository;

import br.com.evolution.kitcomposicao.domain.ItemPedido;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.mgecomercial.model.helper.centrais.CACHelper;
import br.com.sankhya.ws.ServiceContext;
import org.jdom.Element;

import java.math.BigDecimal;
import java.util.List;

/**
 * Inclui itens no pedido pelo mesmo barramento nativo da Central de Vendas:
 * CACHelper.incluirAlterarItem via XML — exatamente a rota do serviço
 * CACSP.incluirAlterarItemNota que a tela usa.
 *
 * Com inicializaProdutos=true o Sankhya aplica todo o pipeline nativo:
 * preço pela regra da TOP, impostos, flags de estoque (RESERVA/ATUALESTOQUE/
 * STATUSNOTA), unidade padrão, regras da Central e validações de liberação —
 * a inserção fica idêntica à digitação manual na tela.
 */
public final class ItemNotaNativoRepository {

    private ItemNotaNativoRepository() {}

    public static int incluirItens(BigDecimal nunota,
                                   BigDecimal codLocalOrig,
                                   List<ItemPedido> itens) throws Exception {

        ServiceContext serviceCtx = ServiceContext.getCurrent();
        if (serviceCtx == null) {
            throw new Exception("Contexto de serviço indisponível para incluir itens pelo fluxo nativo.");
        }

        Element itensElem = new Element("itens");
        for (ItemPedido item : itens) {
            itensElem.addContent(montarItemXML(nunota, codLocalOrig, item));
        }

        CACHelper cacHelper = new CACHelper();
        cacHelper.incluirAlterarItem(nunota, serviceCtx, itensElem, true);
        return itens.size();
    }

    /**
     * Mesmo bypass que o "Sim" da pergunta nativa "Deseja efetivá-lo?" seta na
     * Central: com a propriedade de sessão presente, a validação de estoque
     * (EstoqueHelpper) efetiva o item em vez de lançar a pergunta — que, num
     * botão de ação, reexecutaria a rotina em loop.
     */
    public static void autorizarEfetivacaoSemEstoque(BigDecimal codProd) {
        JapeSession.putProperty("central.notas.pode.efetivar_" + codProd.toPlainString(), "true");
    }

    private static Element montarItemXML(BigDecimal nunota,
                                         BigDecimal codLocalOrig,
                                         ItemPedido item) {
        Element itemElem = new Element("item");
        itemElem.addContent(campo("NUNOTA", nunota));
        // SEQUENCIA vazia = item novo: dispara o PKNullElementError tratado
        // pelo CACHelper, que então cria o VO e gera a sequência nativamente.
        itemElem.addContent(new Element("SEQUENCIA"));
        itemElem.addContent(campo("CODPROD", item.getCodProd()));
        itemElem.addContent(campo("QTDNEG", item.getQtdNeg()));
        itemElem.addContent(campo("CODLOCALORIG", codLocalOrig));
        // VLRUNIT e CODVOL propositalmente não informados: o pipeline nativo
        // resolve preço (regra da TOP) e unidade padrão em inicializaProduto.
        return itemElem;
    }

    private static Element campo(String nome, BigDecimal valor) {
        Element campo = new Element(nome);
        campo.setText(valor.toPlainString());
        return campo;
    }
}
