import Reader.ConversorAlgebra;
import Reader.Sintaxe;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainSwing extends JFrame {

    private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton executarBtn;
    private Sintaxe sintaxe;

    public MainSwing() {
        super("Validador de SELECT");

        sintaxe = new Sintaxe();

        inputArea = new JTextArea(5, 40);
        JScrollPane scrollInput = new JScrollPane(inputArea);

        executarBtn = new JButton("Executar");

        outputArea = new JTextArea(10, 40);
        outputArea.setEditable(false);
        JScrollPane scrollOutput = new JScrollPane(outputArea);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Digite sua consulta SQL:"), BorderLayout.NORTH);
        topPanel.add(scrollInput, BorderLayout.CENTER);
        topPanel.add(executarBtn, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JLabel("Resultado:"), BorderLayout.CENTER);
        add(scrollOutput, BorderLayout.SOUTH);

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
            resultado.append(sintaxe.extrairPartes(consulta)).append("\n");

            ConversorAlgebra conversor = new ConversorAlgebra();
            String algebra = conversor.converterParaAlgebra(consulta, sintaxe);
            resultado.append("\nÁlgebra Relacional:\n").append(algebra);
        }

        outputArea.setText(resultado.toString());
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainSwing());
    }
}


