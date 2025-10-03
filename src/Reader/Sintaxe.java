package Reader;

import java.util.regex.*;

public class Sintaxe {

    private static final String IDENT   = "[a-zA-Z_][a-zA-Z0-9_\\.]*";
    private static final String NUMBER  = "\\d+(?:\\.\\d+)?";
    private static final String STRING  = "'(?:[^']|'')*'";
    private static final String VALUE   = "(?:" + NUMBER + "|" + STRING + "|" + IDENT + ")";
    private static final String OP      = "(?:=|<>|!=|>=|<=|>|<)";
    private static final String ATOM    = IDENT + "\\s*" + OP + "\\s*" + VALUE;

    private static final String CONDITION = ATOM + "(?:\\s+(?:AND|OR)\\s+" + ATOM + ")*";
    private static final String JOIN      = "(?:\\s+JOIN\\s+(" + IDENT + ")\\s+ON\\s+(" + CONDITION + "))*";
    private static final String COLUMNS   = "(\\*|(?:" + IDENT + "(?:\\s*\\.\\s*" + IDENT + ")?)(?:\\s*,\\s*(?:" + IDENT + "(?:\\s*\\.\\s*" + IDENT + ")?))*)";
    private static final String TABLE     = "(" + IDENT + ")";

    private static final String SELECT_REGEX =
            "^\\s*SELECT\\s+" + COLUMNS +                // grupo 1 = colunas
                    "\\s+FROM\\s+" + TABLE +            // grupo 2 = tabela principal
                    JOIN +                              // grupo 3+ = joins (pares de tabela+condição)
                    "(?:\\s+WHERE\\s+(" + CONDITION + "))?" +   // último grupo = condição WHERE
                    "\\s*;?\\s*$";

    private static final Pattern PATTERN = Pattern.compile(SELECT_REGEX, Pattern.CASE_INSENSITIVE);


    public boolean validarSelect(String query) {
        if (query == null || query.isBlank()) return false;
        Matcher m = PATTERN.matcher(query.trim());
        if (!m.matches()) return false;

        return validarTabelasEColunas(m);
    }

    public String extrairPartes(String query) {
        Matcher m = PATTERN.matcher(query.trim());
        if (!m.matches()) return "Query inválida";

        if (!validarTabelasEColunas(m)) {
            return "Query inválida (tabelas ou colunas não existem)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Query válida\n");

        String colunas = m.group(1);
        sb.append("Colunas: ").append(colunas).append("\n");

        String tabela = m.group(2);
        sb.append("Tabela principal: ").append(tabela).append("\n");

        int groupCount = m.groupCount();
        boolean temJoin = false;
        for (int i = 3; i < groupCount; i += 2) {
            String tabelaJoin = m.group(i);
            String condicaoJoin = (i + 1 <= groupCount) ? m.group(i + 1) : null;
            if (tabelaJoin != null) {
                temJoin = true;
                sb.append("Join com tabela: ").append(tabelaJoin);
                if (condicaoJoin != null) {
                    sb.append(" ON ").append(condicaoJoin);
                }
                sb.append("\n");
            }
        }
        if (!temJoin) {
            sb.append("Sem JOIN\n");
        }

        String where = m.group(groupCount);
        if (where != null) {
            sb.append("Condição WHERE: ").append(where).append("\n");
        } else {
            sb.append("Sem WHERE\n");
        }

        return sb.toString();
    }


    private boolean validarTabelasEColunas(Matcher m) {
        String colunasStr = m.group(1);
        String[] colunas = colunasStr.split("\\s*,\\s*");

        String tabelaPrincipal = normalizarNome(m.group(2));
        if (!contémTabela(tabelaPrincipal)) {
            return false;
        }

        for (String coluna : colunas) {
            if (coluna.equals("*")) continue;
            String[] partes = coluna.split("\\.");
            if (partes.length == 2) {
                String tabela = normalizarNome(partes[0]);
                String atributo = normalizarNome(partes[1]);
                if (!contémAtributo(tabela, atributo)) {
                    return false;
                }
            } else {
                if (!contémAtributo(tabelaPrincipal, normalizarNome(coluna))) {
                    return false;
                }
            }
        }

        int groupCount = m.groupCount();
        for (int i = 3; i < groupCount; i += 2) {
            String tabelaJoin = m.group(i);
            if (tabelaJoin != null && !contémTabela(normalizarNome(tabelaJoin))) {
                return false;
            }
        }

        String where = m.group(groupCount);
        if (where != null) {
            Matcher identMatcher = Pattern.compile(IDENT).matcher(where);
            while (identMatcher.find()) {
                String token = identMatcher.group();
                if (token.matches("\\d+")) continue; // número
                if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) continue;

                String[] partes = token.split("\\.");
                if (partes.length == 2) {
                    String tabela = normalizarNome(partes[0]);
                    String atributo = normalizarNome(partes[1]);
                    if (!contémAtributo(tabela, atributo)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private String normalizarNome(String nome) {
        return nome.trim().toLowerCase(); 
    }

    private boolean contémTabela(String tabela) {
        for (String t : Valores.tabelas.keySet()) {
            if (t.equalsIgnoreCase(tabela)) return true;
        }
        return false;
    }

    private boolean contémAtributo(String tabela, String atributo) {
        for (String t : Valores.tabelas.keySet()) {
            if (t.equalsIgnoreCase(tabela)) {
                for (String a : Valores.tabelas.get(t)) {
                    if (a.equalsIgnoreCase(atributo)) return true;
                }
            }
        }
        return false;
    }
}
