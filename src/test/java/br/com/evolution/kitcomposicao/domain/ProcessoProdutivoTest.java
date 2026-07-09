package br.com.evolution.kitcomposicao.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcessoProdutivoTest {

    private static final BigDecimal IDPROC = BigDecimal.valueOf(40);

    @Test
    void processoValidoPassa() {
        ProcessoProdutivo processo = new ProcessoProdutivo(IDPROC, true, 1, 3);

        assertDoesNotThrow(processo::validarParaBuscaDeComposicao);
    }

    @Test
    void processoInativoBloqueia() {
        ProcessoProdutivo processo = new ProcessoProdutivo(IDPROC, false, 1, 3);

        Exception erro = assertThrows(Exception.class, processo::validarParaBuscaDeComposicao);
        assertTrue(erro.getMessage().contains("não está ativo"));
    }

    @Test
    void paSemVinculoAoProcessoBloqueia() {
        ProcessoProdutivo processo = new ProcessoProdutivo(IDPROC, true, 1, 0);

        Exception erro = assertThrows(Exception.class, processo::validarParaBuscaDeComposicao);
        assertTrue(erro.getMessage().contains("não possui vínculo"));
    }

    @Test
    void maisDeUmaAtividadeBloqueia() {
        ProcessoProdutivo processo = new ProcessoProdutivo(IDPROC, true, 2, 3);

        Exception erro = assertThrows(Exception.class, processo::validarParaBuscaDeComposicao);
        assertTrue(erro.getMessage().contains("mais de uma atividade"));
    }

    @Test
    void inativoTemPrecedenciaSobreDemaisRegras() {
        ProcessoProdutivo processo = new ProcessoProdutivo(IDPROC, false, 2, 0);

        Exception erro = assertThrows(Exception.class, processo::validarParaBuscaDeComposicao);
        assertTrue(erro.getMessage().contains("não está ativo"));
    }
}
