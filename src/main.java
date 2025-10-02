import Reader.Sintaxe;
import java.util.Scanner;
public class main {
    public static void main(String[] args) {
        Sintaxe sintaxe = new Sintaxe();
        System.out.println("Digite sua consulta");
        Scanner sc = new Scanner(System.in);
        String resposta = sc.nextLine();
        System.out.println(sintaxe.validarSelect(resposta));
        System.out.println(sintaxe.extrairPartes(resposta));
    }
}
