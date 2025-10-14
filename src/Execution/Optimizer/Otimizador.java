package Execution.Optimizer;

import java.util.*;

/**
 * Classe responsável por aplicar heurísticas de otimização de consulta
 * sobre o plano lógico antes da geração do plano de execução físico.
 *
 * As heurísticas implementadas visam:
 *  1) Reduzir o número de tuplas via push-down de seleções.
 *  2) Reduzir atributos com projeções antecipadas.
 *  3) Reordenar junções para melhorar seletividade.
 *  4) Evitar produtos cartesianos desnecessários.
 */
public class Otimizador {

    public static Map<String, Object> otimizar(Map<String, Object> partes) {
        if (partes == null || partes.isEmpty()) return partes;

        Map<String, Object> partesOtimizado = new HashMap<>(partes);

        // === 1. Push-down de seleções ===
        partesOtimizado = aplicarSelecoesIndividuais(partesOtimizado);

        // === 2. Push-down de projeções ===
        partesOtimizado = aplicarProjecoesPorTabela(partesOtimizado);

        // === 3. Reordenação de junções ===
        partesOtimizado = ordenarJoinsPorRestricao(partesOtimizado);

        // === 4. Evitar produtos cartesianos ===
        partesOtimizado = evitarProdutosCartesianos(partesOtimizado);

        return partesOtimizado;
    }
    /**
     * Heurística 1: Move seleções específicas de cada tabela
     * para o nível mais baixo possível no plano lógico.
     */
    private static Map<String, Object> aplicarSelecoesIndividuais(Map<String, Object> partes) {
        String where = (String) partes.get("where");
        if (where == null || where.isBlank()) return partes;

        Map<String, String> selecoes = new HashMap<>();
        List<String> condicoesJoin = new ArrayList<>();

        // Divide o WHERE em condições simples (apenas por AND, inicialmente)
        String[] condicoes = where.split("(?i)\\s+AND\\s+");

        for (String cond : condicoes) {
            cond = cond.trim();

            // Se envolve duas tabelas (junção)
            if (cond.matches("(?i).*\\w+\\.\\w+\\s*=\\s*\\w+\\.\\w+.*")) {
                condicoesJoin.add(cond);
                continue;
            }

            // Extrai o nome da tabela (se existir)
            String tabela = null;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.(\\w+)").matcher(cond);
            if (m.find()) {
                tabela = m.group(1);
            } else {
                // Se não tiver tabela explícita, aplica à tabela principal
                tabela = (String) partes.get("tabelaPrincipal");
            }

            selecoes.merge(tabela, cond, (a, b) -> a + " AND " + b);
        }

        // Atualiza a estrutura
        partes.put("selecoes", selecoes);

        // Recompõe o WHERE apenas com as junções (condições entre tabelas)
        partes.put("where", String.join(" AND ", condicoesJoin));

        return partes;
    }


    /**
     * Heurística 2: Identifica as colunas utilizadas por cada tabela
     * para limitar os atributos carregados em etapas intermediárias.
     */
    private static Map<String, Object> aplicarProjecoesPorTabela(Map<String, Object> partes) {
        List<String> colunas = (List<String>) partes.get("colunas");
        Map<String, String> selecoes = (Map<String, String>) partes.get("selecoes");
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");

        if (colunas == null || colunas.isEmpty()) return partes;

        Map<String, List<String>> projecoes = new HashMap<>();

        // 1. Associar colunas do SELECT às tabelas
        for (String col : colunas) {
            String tabela = null;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.(\\w+)").matcher(col);
            if (m.find()) {
                tabela = m.group(1);
                col = m.group(2);
            } else {
                tabela = (String) partes.get("tabelaPrincipal");
            }

            projecoes.computeIfAbsent(tabela, k -> new ArrayList<>()).add(col);
        }

        // 2. Incluir colunas usadas em seleções
        if (selecoes != null) {
            for (Map.Entry<String, String> entry : selecoes.entrySet()) {
                String tabela = entry.getKey();
                String cond = entry.getValue();

                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.(\\w+)").matcher(cond);
                while (m.find()) {
                    projecoes.computeIfAbsent(m.group(1), k -> new ArrayList<>())
                            .add(m.group(2));
                }
            }
        }

        // 3. Incluir colunas usadas em junções
        if (joins != null) {
            for (Map<String, String> join : joins) {
                String cond = join.get("condicao");
                if (cond == null) continue;

                java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.(\\w+)").matcher(cond);
                while (m.find()) {
                    projecoes.computeIfAbsent(m.group(1), k -> new ArrayList<>())
                            .add(m.group(2));
                }
            }
        }

        // 4. Remover duplicatas
        for (String t : projecoes.keySet()) {
            List<String> lista = projecoes.get(t);
            Set<String> unicos = new LinkedHashSet<>(lista);
            projecoes.put(t, new ArrayList<>(unicos));
        }

        // 5. Atualizar estrutura
        partes.put("projecoes", projecoes);
        return partes;
    }

    /**
     * Heurística 3: Ordena as junções com base em seletividade
     * e restrições (tabelas com seleções mais restritivas primeiro).
     */
    private static Map<String, Object> ordenarJoinsPorRestricao(Map<String, Object> partes) {
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        Map<String, String> selecoes = (Map<String, String>) partes.get("selecoes");

        if (joins == null || joins.isEmpty()) return partes;
        if (selecoes == null) return partes;

        // 1. Calcula “peso” de cada tabela (quantidade de condições de seleção)
        Map<String, Integer> pesoTabelas = new HashMap<>();
        for (String tabela : selecoes.keySet()) {
            String cond = selecoes.get(tabela);
            int peso = cond.split("(?i)AND").length; // número de condições AND
            pesoTabelas.put(tabela, peso);
        }

        // 2. Ordena os joins por “peso máximo” das tabelas envolvidas
        joins.sort((j1, j2) -> {
            String t1a = j1.get("tabela");
            String t1b = extrairTabelaDaCondicao(j1.get("condicao"));

            String t2a = j2.get("tabela");
            String t2b = extrairTabelaDaCondicao(j2.get("condicao"));

            int peso1 = Math.max(pesoTabelas.getOrDefault(t1a, 0), pesoTabelas.getOrDefault(t1b, 0));
            int peso2 = Math.max(pesoTabelas.getOrDefault(t2a, 0), pesoTabelas.getOrDefault(t2b, 0));

            return Integer.compare(peso2, peso1); // decrescente
        });

        partes.put("joins", joins);
        return partes;
    }

    /**
     * Extrai uma tabela de uma condição de join, assumindo padrão tabela.coluna
     */
    private static String extrairTabelaDaCondicao(String condicao) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.\\w+").matcher(condicao);
        if (m.find()) return m.group(1);
        return null;
    }

    /**
     * Heurística 4: Substitui produtos cartesianos por junções
     * quando houver condições de ligação aplicáveis.
     */
    private static Map<String, Object> evitarProdutosCartesianos(Map<String, Object> partes) {
        List<Map<String, String>> joins = (List<Map<String, String>>) partes.get("joins");
        Map<String, String> selecoes = (Map<String, String>) partes.get("selecoes");
        String where = (String) partes.get("where");

        if (joins == null || joins.isEmpty()) return partes;
        if (where == null || where.isBlank()) return partes;

        // Separar condições WHERE que podem virar joins
        List<String> condicoesRestantes = new ArrayList<>();
        String[] condicoes = where.split("(?i)\\s+AND\\s+");

        for (String cond : condicoes) {
            cond = cond.trim();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\.(\\w+)\\s*=\\s*(\\w+)\\.(\\w+)").matcher(cond);
            if (m.find()) {
                // Encontrou uma condição de join, adiciona como join se ainda não existir
                String tabela1 = m.group(1);
                String tabela2 = m.group(3);

                boolean existe = joins.stream().anyMatch(j ->
                        (j.get("tabela").equalsIgnoreCase(tabela1) || j.get("tabela").equalsIgnoreCase(tabela2))
                );
                if (!existe) {
                    Map<String, String> novoJoin = new HashMap<>();
                    novoJoin.put("tabela", tabela2);
                    novoJoin.put("condicao", cond);
                    joins.add(novoJoin);
                }
            } else {
                // Mantém condição que não é join
                condicoesRestantes.add(cond);
            }
        }

        partes.put("joins", joins);
        partes.put("where", String.join(" AND ", condicoesRestantes));

        return partes;
    }
}
