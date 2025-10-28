package Reader;

import Execution.Optimizer.Otimizador;
import java.util.*;

public class ConversorAlgebra {

    public String converterParaAlgebra(String query, Sintaxe parser) {
        if (!parser.validarSelect(query)) {
            return "Consulta inválida para conversão.";
        }

        Map<String, Object> partesOriginais = parser.parseQuery(query);
        if (partesOriginais == null || partesOriginais.isEmpty()) {
            return "Erro ao interpretar a consulta.";
        }

        Map<String, Object> partes = Otimizador.otimizar(partesOriginais);

        String tabelaPrincipal = (String) partes.get("tabelaPrincipal");
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        Map<String, String> selecoes = (Map<String, String>) partes.get("selecoes");
        Map<String, List<String>> projecoes = (Map<String, List<String>>) partes.get("projecoes");
        List<String> colunas = (List<String>) partes.get("colunas");

        StringBuilder algebra = new StringBuilder();
        algebra.append("π ").append(String.join(", ", colunas)).append(" (");

        String corpo = construirBlocoTabela(tabelaPrincipal, selecoes, projecoes);

        if (joins != null && !joins.isEmpty()) {
            for (Map<String, String> join : joins) {
                String tabelaJoin = join.get("tabela");
                String condicao = join.get("condicao");
                String blocoJoin = construirBlocoTabela(tabelaJoin, selecoes, projecoes);
                corpo = "(" + corpo + " ⋈_{" + condicao + "} " + blocoJoin + ")";
            }
        }

        algebra.append(corpo).append(")");
        return algebra.toString();
    }

    private String construirBlocoTabela(String tabela,
                                        Map<String, String> selecoes,
                                        Map<String, List<String>> projecoes) {

        String base = tabela;

        if (selecoes != null && selecoes.containsKey(tabela)) {
            base = "σ " + selecoes.get(tabela) + " (" + base + ")";
        }

        if (projecoes != null && projecoes.containsKey(tabela)) {
            base = "π " + String.join(", ", projecoes.get(tabela)) + " (" + base + ")";
        }

        return base;
    }
}
