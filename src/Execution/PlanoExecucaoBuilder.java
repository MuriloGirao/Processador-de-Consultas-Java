package Execution;

import java.util.List;
import java.util.Map;

public class PlanoExecucaoBuilder {

    public static PlanoExecucao build(Map<String, Object> partes) {
        PlanoExecucao plano = new PlanoExecucao();

        if (partes == null || partes.isEmpty()) return plano;

        String tabelaPrincipal = (String) partes.get("tabelaPrincipal");
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        String where = (String) partes.get("where");
        List<String> colunas = (List<String>) partes.get("colunas");

        // === 1. Nó base (tabela principal) ===
        NodoPlano tabelaNodo = new NodoPlano("N1", tabelaPrincipal, NodoPlano.Tipo.TABELA);
        plano.adicionarNodo(tabelaNodo);
        plano.adicionarPasso("1) Tabela base: " + tabelaPrincipal);

        String ultimoId = tabelaNodo.getId();

        // === 2. Produto cartesiano (para joins) ===
        if (joins != null && !joins.isEmpty()) {
            for (Map<String, String> j : joins) {
                String tabelaJoin = j.get("tabela");
                String condicao = j.get("condicao");

                NodoPlano tabelaJoinNodo = new NodoPlano(
                        "N" + plano.getProximoId(),
                        tabelaJoin,
                        NodoPlano.Tipo.TABELA
                );
                plano.adicionarNodo(tabelaJoinNodo);

                NodoPlano joinNodo = new NodoPlano(
                        "N" + plano.getProximoId(),
                        "⋈ " + condicao,
                        NodoPlano.Tipo.CARTESIANO
                );
                plano.adicionarNodo(joinNodo);

                plano.adicionarAresta(ultimoId, joinNodo.getId());
                plano.adicionarAresta(tabelaJoinNodo.getId(), joinNodo.getId());

                plano.adicionarPasso("2) Join entre " + tabelaPrincipal + " e " + tabelaJoin + " com condição: " + condicao);
                ultimoId = joinNodo.getId();
            }
        }

        // === 3. Seleção (WHERE) ===
        if (where != null && !where.isBlank()) {
            NodoPlano selecaoNodo = new NodoPlano(
                    "N" + plano.getProximoId(),
                    "σ " + where,
                    NodoPlano.Tipo.SELECAO
            );
            plano.adicionarNodo(selecaoNodo);
            plano.adicionarAresta(ultimoId, selecaoNodo.getId());
            plano.adicionarPasso("3) Seleção: " + where);
            ultimoId = selecaoNodo.getId();
        }

        // === 4. Projeção (SELECT) ===
        if (colunas != null && !colunas.isEmpty()) {
            NodoPlano projecaoNodo = new NodoPlano(
                    "N" + plano.getProximoId(),
                    "π " + String.join(", ", colunas),
                    NodoPlano.Tipo.PROJECAO
            );
            plano.adicionarNodo(projecaoNodo);
            plano.adicionarAresta(ultimoId, projecaoNodo.getId());
            plano.adicionarPasso("4) Projeção: " + String.join(", ", colunas));
        }

        return plano;
    }
}
