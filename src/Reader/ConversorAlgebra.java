package Reader;

import java.util.List;
import java.util.Map;

public class ConversorAlgebra {

    public String converterParaAlgebra(String query, Sintaxe parser) {
        // 1. Validar novamente
        if (!parser.validarSelect(query)) {
            return "Consulta inválida para conversão";
        }

        // 2. Extrair partes da consulta
        Map<String, Object> partes = parser.parseQuery(query);

        String tabelaPrincipal = (String) partes.get("tabelaPrincipal");
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        String where = (String) partes.get("where");
        List<String> colunas = (List<String>) partes.get("colunas");

        // 3. Montar álgebra relacional
        StringBuilder algebra = new StringBuilder();

        // projeção (π)
        algebra.append("π ");
        algebra.append(String.join(", ", colunas));
        algebra.append(" (");

        // seleção (σ)
        if (where != null && !where.isBlank()) {
            algebra.append("σ ").append(where).append(" (");
        }

        // junções (⋈)
        String relacao = tabelaPrincipal;
        for (Map<String, String> j : joins) {
            relacao = "(" + relacao + " ⋈ " + j.get("condicao") + " " + j.get("tabela") + ")";
        }

        algebra.append(relacao);

        if (where != null && !where.isBlank()) algebra.append(")");
        algebra.append(")");

        return algebra.toString();
    }
}
