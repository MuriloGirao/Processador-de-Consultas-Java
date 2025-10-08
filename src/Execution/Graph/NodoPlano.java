package Execution.Graph;


public class NodoPlano {

    public enum Tipo {
        TABELA,
        CARTESIANO,
        SELECAO,
        PROJECAO
    }

    private String id;
    private String label;
    private Tipo tipo;

    public NodoPlano(String id, String label, Tipo tipo) {
        this.id = id;
        this.label = label;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
