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

        String resultado = "";
        resultado += "Validação:\n" + sintaxe.validarSelect(consulta) + "\n\n";
        resultado += "Partes extraídas:\n" + sintaxe.extrairPartes(consulta) + "\n";

        outputArea.setText(resultado);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainSwing());
    }
}
