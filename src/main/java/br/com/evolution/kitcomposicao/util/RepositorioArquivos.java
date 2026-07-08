package br.com.evolution.kitcomposicao.util;

import java.lang.reflect.Method;

/**
 * Resolve o caminho de log dentro do SW Repository (Repositório de Arquivos
 * do Sankhya), na pasta personalizacao/evolution.
 *
 * SWRepositoryUtils (mge-modelcore) é acessado por reflection para não exigir
 * o JAR em tempo de compilação — em runtime no servidor a classe sempre existe.
 * Fora do servidor (testes locais), cai no diretório temporário da JVM.
 */
public final class RepositorioArquivos {

    private static final String CLASSE_SW_REPOSITORY = "br.com.sankhya.modelcore.util.SWRepositoryUtils";
    private static final String PASTA_LOG = "/personalizacao/evolution";

    private RepositorioArquivos() {}

    public static String pastaDeLog() {
        return baseFolder() + PASTA_LOG;
    }

    private static String baseFolder() {
        try {
            Class<?> swRepository = Class.forName(CLASSE_SW_REPOSITORY);
            Method getBaseFolder = swRepository.getMethod("getBaseFolder");
            Object base = getBaseFolder.invoke(null);
            if (base != null) {
                return base.toString();
            }
        } catch (Exception fallbackParaTmp) {
            // fora do servidor Sankhya (ex.: teste local) a classe não existe
        }
        return System.getProperty("java.io.tmpdir");
    }
}
