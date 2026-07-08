package br.com.evolution.kitcomposicao.util;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;

import java.math.BigDecimal;

public final class ParamUtil {

    private ParamUtil() {
        // evita instanciar
    }

    /* ===================== STRING ===================== */
    public static String getStringObrigatorio(ContextoAcao ctx, String nome, String label) throws Exception {
        Object v = ctx.getParam(nome);
        if (v == null) {
            throw new Exception("Atenção! Campo obrigatório não informado: " + label + ".");
        }

        String s = v.toString().trim();
        if (s.isEmpty()) {
            throw new Exception("Atenção! Campo obrigatório em branco: " + label + ".");
        }

        return s;
    }

    public static String getStringOpcional(ContextoAcao ctx, String nome) {
        Object v = ctx.getParam(nome);
        if (v == null) return null;

        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /* ===================== BIGDECIMAL ===================== */
    public static BigDecimal getBigDecimalObrigatorio(ContextoAcao ctx, String nome, String label) throws Exception {
        BigDecimal bd = toBigDecimal(ctx.getParam(nome));
        if (bd == null) {
            throw new Exception("Atenção! Campo obrigatório não informado: " + label + ".");
        }
        return bd;
    }

    public static BigDecimal getBigDecimalOpcional(ContextoAcao ctx, String nome) throws Exception {
        return toBigDecimal(ctx.getParam(nome));
    }

    /* ===================== BOOLEAN ===================== */
    public static boolean getBooleanOpcional(ContextoAcao ctx, String nome) {
        Object v = ctx.getParam(nome);
        if (v == null) return false;

        if (v instanceof Boolean) {
            return (Boolean) v;
        }

        String s = v.toString().trim();
        return "S".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s);
    }

    private static BigDecimal toBigDecimal(Object v) throws Exception {
        if (v == null) return null;

        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }

        String s = v.toString().trim();
        if (s.isEmpty()) return null;

        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new Exception("Valor numérico inválido: " + s);
        }
    }
}
