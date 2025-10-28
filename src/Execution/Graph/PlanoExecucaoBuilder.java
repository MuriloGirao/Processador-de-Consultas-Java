package Execution.Graph;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlanoExecucaoBuilder {


    public static PlanoExecucao buildFromAlgebra(String algebra) {
        PlanoExecucao plano = new PlanoExecucao();
        if (algebra == null || algebra.isBlank()) return plano;

        // Normaliza espaços e parênteses
        algebra = algebra.replaceAll("\\s+", " ").trim();

        // Chamada recursiva para construir o grafo
        construirRec(plano, algebra, null);
        return plano;
    }


    private static String construirRec(PlanoExecucao plano, String expr, String parentId) {
        expr = expr.trim();

        if (!expr.contains("π") && !expr.contains("σ") && !expr.contains("⋈")) {
            NodoPlano n = new NodoPlano("N" + plano.getProximoId(), expr, NodoPlano.Tipo.TABELA);
            plano.adicionarNodo(n);
            if (parentId != null) plano.adicionarAresta(n.getId(), parentId);
            plano.adicionarPasso("Tabela base: " + expr);
            return n.getId();
        }

        if (expr.startsWith("π")) {
            Matcher m = Pattern.compile("π (.*?) \\((.*)\\)").matcher(expr);
            if (m.find()) {
                String cols = m.group(1).trim();
                String subexpr = m.group(2).trim();
                NodoPlano proj = new NodoPlano("N" + plano.getProximoId(), "π " + cols, NodoPlano.Tipo.PROJECAO);
                plano.adicionarNodo(proj);
                plano.adicionarPasso("Projeção: " + cols);
                String child = construirRec(plano, subexpr, proj.getId());
                if (parentId != null) plano.adicionarAresta(proj.getId(), parentId);
                return proj.getId();
            }
        }

        if (expr.startsWith("σ")) {
            Matcher m = Pattern.compile("σ (.*?) \\((.*)\\)").matcher(expr);
            if (m.find()) {
                String cond = m.group(1).trim();
                String subexpr = m.group(2).trim();
                NodoPlano sel = new NodoPlano("N" + plano.getProximoId(), "σ " + cond, NodoPlano.Tipo.SELECAO);
                plano.adicionarNodo(sel);
                plano.adicionarPasso("Seleção: " + cond);
                String child = construirRec(plano, subexpr, sel.getId());
                if (parentId != null) plano.adicionarAresta(sel.getId(), parentId);
                return sel.getId();
            }
        }

        if (expr.contains("⋈")) {
            Matcher m = Pattern.compile("\\((.*)⋈_\\{(.*?)}(.*)\\)").matcher(expr);
            if (m.find()) {
                String leftExpr = m.group(1).trim();
                String cond = m.group(2).trim();
                String rightExpr = m.group(3).trim();

                NodoPlano join = new NodoPlano("N" + plano.getProximoId(), "⋈ {" + cond + "}", NodoPlano.Tipo.CARTESIANO);
                plano.adicionarNodo(join);
                plano.adicionarPasso("Junção: " + cond);

                String left = construirRec(plano, limparParenteses(leftExpr), join.getId());
                String right = construirRec(plano, limparParenteses(rightExpr), join.getId());

                if (parentId != null) plano.adicionarAresta(join.getId(), parentId);
                return join.getId();
            }
        }

        NodoPlano n = new NodoPlano("N" + plano.getProximoId(), expr, NodoPlano.Tipo.TABELA);
        plano.adicionarNodo(n);
        if (parentId != null) plano.adicionarAresta(n.getId(), parentId);
        plano.adicionarPasso("Expressão genérica: " + expr);
        return n.getId();
    }


    private static String limparParenteses(String expr) {
        expr = expr.trim();
        while (expr.startsWith("(") && expr.endsWith(")") && parentesesBalanceados(expr.substring(1, expr.length() - 1))) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }
        return expr;
    }

    private static boolean parentesesBalanceados(String s) {
        int count = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') count++;
            else if (c == ')') count--;
            if (count < 0) return false;
        }
        return count == 0;
    }
}
