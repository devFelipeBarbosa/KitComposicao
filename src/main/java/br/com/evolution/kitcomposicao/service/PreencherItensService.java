package br.com.evolution.kitcomposicao.service;

import br.com.evolution.kitcomposicao.domain.ComposicaoPA;
import br.com.evolution.kitcomposicao.domain.ItemComposicao;
import br.com.evolution.kitcomposicao.domain.ItemPedido;
import br.com.evolution.kitcomposicao.domain.ProcessoProdutivo;
import br.com.evolution.kitcomposicao.domain.RegraEstoquePedido;
import br.com.evolution.kitcomposicao.domain.ResultadoInclusao;
import br.com.evolution.kitcomposicao.repository.ComposicaoProdutoRepository;
import br.com.evolution.kitcomposicao.repository.ProcessoProdutivoRepository;
import br.com.evolution.kitcomposicao.repository.ItemNotaNativoRepository;
import br.com.evolution.kitcomposicao.repository.PedidoRepository;
import br.com.evolution.kitcomposicao.repository.ProdutoRepository;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.mgecomercial.model.utils.ComercialUtils;
import br.com.sankhya.modelcore.comercial.proxyconnect.IResultadoValidaEstoque;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Caso de uso: buscar a Composição de Produto do PA, validar estoque item a
 * item como na digitação manual (SP nativa Stp_Valida_Estoque40, perguntando
 * "Deseja efetivá-lo?" por produto sem estoque) e incluir os itens aceitos
 * pelo barramento nativo da Central.
 */
public class PreencherItensService {

    private static final String ERRO_PA_SEM_COMPOSICAO =
        "Item (PA) não encontrado. Por gentileza, providenciar o cadastro da Composição de Produto!";

    public ResultadoInclusao executar(ContextoAcao ctx,
                                      BigDecimal nunota,
                                      BigDecimal codProdPA,
                                      BigDecimal codLocalOrig,
                                      BigDecimal qtdDigitada,
                                      boolean listarPA,
                                      BigDecimal idProc) throws Exception {

        ProcessoProdutivo processo =
            ProcessoProdutivoRepository.buscar(ctx.getQuery(), idProc, codProdPA);
        processo.validarParaBuscaDeComposicao();

        List<ItemComposicao> materiasPrimas =
            ComposicaoProdutoRepository.buscarMateriasPrimas(ctx.getQuery(), codProdPA, idProc);

        if (materiasPrimas.isEmpty()) {
            throw new Exception(ERRO_PA_SEM_COMPOSICAO);
        }

        ComposicaoPA composicao = new ComposicaoPA(codProdPA, materiasPrimas);
        List<ItemPedido> itens = composicao.gerarItensPedido(qtdDigitada, listarPA);
        ordenarPorProduto(itens);

        List<ItemPedido> aceitos = validarEstoqueItemAItem(ctx, nunota, codLocalOrig, itens);

        if (aceitos.isEmpty()) {
            throw new Exception("Nenhum item incluído: inclusão sem estoque foi recusada para todos os itens.");
        }

        int incluidos = ItemNotaNativoRepository.incluirItens(nunota, codLocalOrig, aceitos);
        return new ResultadoInclusao(incluidos, itens.size() - incluidos);
    }

    /**
     * Espelho da digitação manual: valida cada item pela SP nativa e, quando a
     * TOP está configurada para avisar ('A'), pergunta por produto. "Sim"
     * autoriza a efetivação (mesmo bypass do "Sim" da tela); "Não" pula o item.
     * A pergunta é feita aqui, antes do CACHelper, para a validação nativa não
     * disparar o evento de tela — que reexecutaria o botão em loop.
     */
    private List<ItemPedido> validarEstoqueItemAItem(ContextoAcao ctx,
                                                     BigDecimal nunota,
                                                     BigDecimal codLocalOrig,
                                                     List<ItemPedido> itens) throws Exception {
        RegraEstoquePedido regra = PedidoRepository.buscarRegraEstoque(ctx.getQuery(), nunota);

        List<ItemPedido> aceitos = new ArrayList<ItemPedido>();
        int indicePergunta = 1;

        for (ItemPedido item : itens) {
            String usoProd = ProdutoRepository.buscarUsoProd(ctx.getQuery(), item.getCodProd());

            if (!regra.deveValidarEstoque(usoProd)) {
                aceitos.add(item);
                continue;
            }

            IResultadoValidaEstoque estoque = ComercialUtils.validaEstoque(
                regra.getCodEmp(), codLocalOrig, item.getCodProd(),
                null, regra.getStatusBaixaEst(), regra.eTopDeReserva());

            String validacao = estoque.getValEst();
            if ("I".equals(validacao)) {
                throw new Exception("Produto " + item.getCodProd()
                    + " inativo ou inexistente na tabela de Estoque.");
            }

            boolean estoqueSuficiente = "N".equals(validacao)
                || valorOuZero(estoque.getQtdEst()).compareTo(item.getQtdNeg()) >= 0;

            if (estoqueSuficiente) {
                aceitos.add(item);
                continue;
            }

            if (!"A".equals(validacao)) {
                throw new Exception("Estoque do produto " + item.getCodProd()
                    + " insuficiente. Cancelando operação.");
            }

            boolean efetivar = ctx.confirmarSimNao("Confirmação",
                "Não há estoque suficiente para \"" + item.getCodProd()
                    + "\". Deseja efetivá-lo?", indicePergunta++);

            if (efetivar) {
                ItemNotaNativoRepository.autorizarEfetivacaoSemEstoque(item.getCodProd());
                aceitos.add(item);
            }
        }
        return aceitos;
    }

    /** Ordem estável: os índices das perguntas se repetem entre reexecuções da ação. */
    private void ordenarPorProduto(List<ItemPedido> itens) {
        itens.sort(new Comparator<ItemPedido>() {
            @Override
            public int compare(ItemPedido a, ItemPedido b) {
                return a.getCodProd().compareTo(b.getCodProd());
            }
        });
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return (valor != null) ? valor : BigDecimal.ZERO;
    }
}
