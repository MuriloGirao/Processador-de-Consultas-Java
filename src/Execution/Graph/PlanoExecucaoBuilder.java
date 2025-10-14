package Execution.Graph;

import Execution.Optimizer.Otimizador;

import java.util.List;
import java.util.Map;

public class PlanoExecucaoBuilder {

    public static PlanoExecucao build(Map<String, Object> partes) {
        PlanoExecucao plano = new PlanoExecucao();

        if (partes == null || partes.isEmpty()) return plano;

        // === 0. Aplicar heurísticas de otimização ===
        partes = Otimizador.otimizar(partes);

        String tabelaPrincipal = (String) partes.get("tabelaPrincipal");
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        Map<String, String> selecoes = (Map<String, String>) partes.get("selecoes"); // H1
        Map<String, List<String>> projecoes = (Map<String, List<String>>) partes.get("projecoes"); // H2

        List<String> colunasGlobais = (List<String>) partes.get("colunas"); // projeção final

        // === 1. Nó base (tabela principal) ===
        NodoPlano tabelaNodo = new NodoPlano("N1", tabelaPrincipal, NodoPlano.Tipo.TABELA);
        plano.adicionarNodo(tabelaNodo);
        plano.adicionarPasso("1) Tabela base: " + tabelaPrincipal);

        String ultimoId = tabelaNodo.getId();

        // === 1b. Projeção antecipada da tabela principal (H2) ===
        if (projecoes != null && projecoes.containsKey(tabelaPrincipal)) {
            List<String> cols = projecoes.get(tabelaPrincipal);
            NodoPlano projNodo = new NodoPlano(
                    "N" + plano.getProximoId(),
                    "π " + String.join(", ", cols),
                    NodoPlano.Tipo.PROJECAO
            );
            plano.adicionarNodo(projNodo);
            plano.adicionarAresta(ultimoId, projNodo.getId());
            plano.adicionarPasso("1b) Projeção antecipada tabela " + tabelaPrincipal + ": " + String.join(", ", cols));
            ultimoId = projNodo.getId();
        }

        // === 2. Produto cartesiano / joins ===
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

                // Projeção antecipada da tabela do join (H2)
                if (projecoes != null && projecoes.containsKey(tabelaJoin)) {
                    List<String> colsJoin = projecoes.get(tabelaJoin);
                    NodoPlano projJoinNodo = new NodoPlano(
                            "N" + plano.getProximoId(),
                            "π " + String.join(", ", colsJoin),
                            NodoPlano.Tipo.PROJECAO
                    );
                    plano.adicionarNodo(projJoinNodo);
                    plano.adicionarAresta(tabelaJoinNodo.getId(), projJoinNodo.getId());
                    plano.adicionarPasso("2b) Projeção antecipada tabela " + tabelaJoin + ": " + String.join(", ", colsJoin));
                    tabelaJoinNodo = projJoinNodo; // agora o join usa o nodo projetado
                }

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

        // === 3. Seleções restantes (H1) ===
        if (selecoes != null) {
            for (Map.Entry<String, String> entry : selecoes.entrySet()) {
                NodoPlano selecaoNodo = new NodoPlano(
                        "N" + plano.getProximoId(),
                        "σ " + entry.getValue(),
                        NodoPlano.Tipo.SELECAO
                );
                plano.adicionarNodo(selecaoNodo);
                plano.adicionarAresta(ultimoId, selecaoNodo.getId());
                plano.adicionarPasso("3) Seleção em " + entry.getKey() + ": " + entry.getValue());
                ultimoId = selecaoNodo.getId();
            }
        }

        // === 4. Projeção final ===
        if (colunasGlobais != null && !colunasGlobais.isEmpty()) {
            NodoPlano projFinal = new NodoPlano(
                    "N" + plano.getProximoId(),
                    "π " + String.join(", ", colunasGlobais),
                    NodoPlano.Tipo.PROJECAO
            );
            plano.adicionarNodo(projFinal);
            plano.adicionarAresta(ultimoId, projFinal.getId());
            plano.adicionarPasso("4) Projeção final: " + String.join(", ", colunasGlobais));
        }

        return plano;
    }
}
