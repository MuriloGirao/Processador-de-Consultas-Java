package Reader;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            "^\\s*SELECT\\s+" + COLUMNS +
                    "\\s+FROM\\s+" + TABLE +
                    JOIN +
                    "(?:\\s+WHERE\\s+(" + CONDITION + "))?" +
                    "\\s*;?\\s*$";

    private static final Pattern PATTERN = Pattern.compile(SELECT_REGEX, Pattern.CASE_INSENSITIVE);



    /**
     * Valida apenas a estrutura sintática básica do SELECT.
     */
    public boolean validarSelect(String query) {
        if (query == null || query.isBlank()) return false;
        return PATTERN.matcher(query.trim()).matches();
    }

    /**
     * Retorna uma estrutura organizada da query para o otimizador.
     * Não executa validação semântica (isso será feito em outra camada).
     */
    public Map<String, Object> parseQuery(String query) {
        Map<String, Object> partes = new HashMap<>();
        Matcher m = PATTERN.matcher(query.trim());
        if (!m.matches()) return partes;

        partes.put("colunas", extrairColunas(m.group(1)));
        partes.put("tabelaPrincipal", normalizar(m.group(2)));
        partes.put("joins", extrairJoins(query));
        partes.put("where", extrairWhere(query));
        return partes;
    }


    // -----------------------------
    // MÉTODOS AUXILIARES
    // -----------------------------

    private List<String> extrairColunas(String colunasStr) {
        return Arrays.asList(colunasStr.split("\\s*,\\s*"));
    }

    private List<Map<String, String>> extrairJoins(String query) {
        List<Map<String, String>> joins = new ArrayList<>();
        Matcher joinMatcher = Pattern.compile("JOIN\\s+(" + IDENT + ")\\s+ON\\s+(" + CONDITION + ")", Pattern.CASE_INSENSITIVE)
                .matcher(query);
        while (joinMatcher.find()) {
            Map<String, String> join = new HashMap<>();
            join.put("tabela", normalizar(joinMatcher.group(1)));
            join.put("condicao", joinMatcher.group(2).trim());
            joins.add(join);
        }
        return joins;
    }

    private String extrairWhere(String query) {
        Matcher whereMatcher = Pattern.compile("WHERE\\s+(" + CONDITION + ")", Pattern.CASE_INSENSITIVE)
                .matcher(query);
        return whereMatcher.find() ? whereMatcher.group(1).trim() : null;
    }

    private String normalizar(String nome) {
        return nome == null ? null : nome.trim().toLowerCase();
    }
}
