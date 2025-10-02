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
                    "\\s+FROM\\s+" + TABLE +                    // grupo 2 = tabela principal
                    JOIN +                                      // grupo 3+ = joins (pares de tabela+condição)
                    "(?:\\s+WHERE\\s+(" + CONDITION + "))?" +   // último grupo = condição WHERE
                    "\\s*;?\\s*$";

    private static final Pattern PATTERN = Pattern.compile(SELECT_REGEX, Pattern.CASE_INSENSITIVE);

    public boolean validarSelect(String query) {
        if (query == null || query.isBlank()) return false;
        return PATTERN.matcher(query.trim()).matches();
    }

    public String extrairPartes(String query) {
        Matcher m = PATTERN.matcher(query.trim());
        if (!m.matches()) return "Query inválida";

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

}
