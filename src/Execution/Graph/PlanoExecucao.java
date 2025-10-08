package Execution.Graph;

import java.util.ArrayList;
import java.util.List;


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
}
