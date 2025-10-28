package Execution.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlanoExecucao {

    private List<NodoPlano> nodos;
    private List<ArestaPlano> arestas;
    private List<String> passos;

    public PlanoExecucao() {
        this.nodos = new ArrayList<>();
        this.arestas = new ArrayList<>();
        this.passos = new ArrayList<>();
    }

    public void adicionarNodo(NodoPlano nodo) {
        nodos.add(nodo);
    }

    public void adicionarAresta(String from, String to) {
        arestas.add(new ArestaPlano(from, to));
    }

    public void adicionarPasso(String passo) {
        passos.add(passo);
    }

    public List<NodoPlano> getNodos() {
        return nodos;
    }

    public List<ArestaPlano> getArestas() {
        return arestas;
    }

    public List<String> getPassosOrdenados() {
        return passos;
    }

    public int getProximoId() {
        return nodos.size() + 1;
    }

    private NodoPlano getNodoById(String id) {
        for (NodoPlano n : nodos) {
            if (n.getId().equals(id)) return n;
        }
        return null;
    }

    public String getPlanoHierarquico() {
        if (nodos.isEmpty()) return "(vazio)";

        // 1. Encontra o nó RAIZ (aquele que ninguém aponta PARA ele)
        Set<String> origens = new HashSet<>();
        for (ArestaPlano a : arestas) origens.add(a.getFrom());

        NodoPlano raiz = null;
        for (NodoPlano n : nodos) {
            if (!origens.contains(n.getId())) {
                raiz = n;
                break;
            }
        }

        if (raiz == null) raiz = nodos.get(nodos.size() - 1);

        StringBuilder sb = new StringBuilder();
        imprimirRecursivo(sb, raiz, 0);
        return sb.toString();
    }

    private void imprimirRecursivo(StringBuilder sb, NodoPlano nodo, int nivel) {
        sb.append("  ".repeat(nivel))
                .append("- ")
                .append(nodo.getLabel())
                .append("\n");

        for (ArestaPlano a : arestas) {
            if (a.getFrom().equals(nodo.getId())) {
                NodoPlano filho = getNodoById(a.getTo());
                if (filho != null)
                    imprimirRecursivo(sb, filho, nivel + 1);
            }
        }
    }
}
