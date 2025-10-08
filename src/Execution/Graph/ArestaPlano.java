package Execution.Graph;

/**
 * Representa uma conexão (aresta) entre dois nós no grafo de execução.
 */
public class ArestaPlano {

    private String from;
    private String to;

    public ArestaPlano(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
