import Execution.Graph.ArestaPlano;
import Execution.Graph.NodoPlano;
import Execution.Graph.PlanoExecucao;
import Execution.Graph.PlanoExecucaoBuilder;
import Reader.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class MainSwing extends JFrame {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton executarBtn;
    private Sintaxe sintaxe;
    private PainelGrafoPlano painelGrafo;

    public MainSwing() {
        super("Validador de SELECT + Plano de Execução");

        sintaxe = new Sintaxe();

        inputArea = new JTextArea(5, 40);
        JScrollPane scrollInput = new JScrollPane(inputArea);

        executarBtn = new JButton("Executar");

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollOutput = new JScrollPane(outputArea);

        painelGrafo = new PainelGrafoPlano();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Digite sua consulta SQL:"), BorderLayout.NORTH);
        topPanel.add(scrollInput, BorderLayout.CENTER);
        topPanel.add(executarBtn, BorderLayout.SOUTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(new JLabel("Resultado:"), BorderLayout.NORTH);
        centro.add(scrollOutput, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centro, painelGrafo);
        splitPane.setDividerLocation(250);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        executarBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executarConsulta();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void executarConsulta() {
        String consulta = inputArea.getText().trim();
        if (consulta.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Digite uma consulta primeiro!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder resultado = new StringBuilder();
        boolean valida = sintaxe.validarSelect(consulta);

        resultado.append("Validação: ").append(valida ? "OK" : "Falhou").append("\n\n");

        if (valida) {
            // ✅ Usa o novo método parseQuery
            Map<String, Object> partes = sintaxe.parseQuery(consulta);

            if (partes.isEmpty()) {
                resultado.append("Erro ao interpretar a consulta.\n");
                painelGrafo.setPlano(null);
                outputArea.setText(resultado.toString());
                return;
            }

            // Mostra resultado parseado
            resultado.append("Partes extraídas:\n").append(partes).append("\n");

            // Conversão para álgebra relacional
            ConversorAlgebra conversor = new ConversorAlgebra();
            String algebra = conversor.converterParaAlgebra(consulta, sintaxe);
            resultado.append("\nÁlgebra Relacional:\n").append(algebra);

            // Geração de plano de execução
            PlanoExecucao plano = PlanoExecucaoBuilder.build(partes);
            resultado.append("\n\nPlano de Execução:\n");
            for (String passo : plano.getPassosOrdenados()) {
                resultado.append(passo).append("\n");
            }

            painelGrafo.setPlano(plano);
        } else {
            painelGrafo.setPlano(null);
        }

        outputArea.setText(resultado.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainSwing::new);
    }
}

class PainelGrafoPlano extends JPanel {

    private PlanoExecucao plano;

    public PainelGrafoPlano() {
        setPreferredSize(new Dimension(800, 400));
        setBackground(Color.WHITE);
    }

    public void setPlano(PlanoExecucao plano) {
        this.plano = plano;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (plano == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int yTabela = height - 60;
        int yCartesiano = height - 150;
        int ySelecao = height - 250;
        int yProjecao = height - 350;

        // Armazena posição de cada nodo
        java.util.Map<String, Point> pos = new java.util.HashMap<>();

        // Distribuição horizontal dinâmica
        int xStep = Math.max(120, width / Math.max(1, plano.getNodos().size()));
        int x = 60;

        // Primeiro pass: posiciona tabelas e seleções logo acima da tabela
        for (NodoPlano n : plano.getNodos()) {
            int y;
            switch (n.getTipo()) {
                case TABELA -> y = yTabela;
                case SELECAO -> y = ySelecao;
                case CARTESIANO -> y = yCartesiano;
                case PROJECAO -> y = yProjecao;
                default -> y = height / 2;
            }

            int boxW = 100;
            int boxH = 40;

            pos.put(n.getId(), new Point(x, y));

            Color fill;
            switch (n.getTipo()) {
                case TABELA -> fill = new Color(180, 220, 255);
                case SELECAO -> fill = new Color(200, 255, 200);
                case CARTESIANO -> fill = new Color(255, 220, 180);
                case PROJECAO -> fill = new Color(255, 255, 180);
                default -> fill = Color.LIGHT_GRAY;
            }

            g2.setColor(fill);
            g2.fillRect(x, y, boxW, boxH);
            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, boxW, boxH);
            g2.drawString(n.getLabel(), x + 5, y + 25);

            x += xStep;
        }

        // Desenha arestas conectando os nós de acordo com a hierarquia
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(Color.DARK_GRAY);

        for (ArestaPlano a : plano.getArestas()) {
            Point p1 = pos.get(a.getFrom());
            Point p2 = pos.get(a.getTo());
            if (p1 != null && p2 != null) {
                // Conecta centros dos nós
                int x1 = p1.x + 50; // boxW / 2
                int y1 = p1.y + 20; // boxH / 2
                int x2 = p2.x + 50;
                int y2 = p2.y + 20;

                g2.drawLine(x1, y1, x2, y2);
            }
        }
    }

}
