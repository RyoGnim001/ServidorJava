import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Servidor {

    public static void main(String[] args) throws IOException{
        ServerSocket servidor = new ServerSocket(12345);
        System.out.println("comecou. aguardando...");
        Socket conexao1 = servidor.accept();
        System.out.println(conexao1.getInetAddress().getHostAddress());
        Scanner LE_DO_SOCKET = new Scanner(conexao1.getInputStream());
        PrintStream ESCREVE_NO_SOCKET = new PrintStream(conexao1.getOutputStream());
        while(LE_DO_SOCKET.hasNextLine()){
            ///ler do socket
            String temp = LE_DO_SOCKET.nextLine();
            System.out.println(temp);
            //escrever no socket
            ESCREVE_NO_SOCKET.println("Resposta da mensagem: "+temp+" = BLZ!!");
        }
        LE_DO_SOCKET.close();
        servidor.close();
        conexao1.close();
    }
}
