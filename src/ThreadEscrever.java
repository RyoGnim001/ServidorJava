import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ThreadEscrever implements Runnable {
    private final Socket socket;
    private final BlockingQueue<String> filaRespostas;

    public ThreadEscrever(Conexao conexao) {
        this.socket = conexao.cliente();
        this.filaRespostas = conexao.filaRepostas();
    }

    @Override
    public void run() {
        try (PrintWriter saida = new PrintWriter(socket.getOutputStream(), true)) {

            while (true) {
                // take() bloqueia até ter algo na fila
                String resposta = filaRespostas.take();

                if (resposta.equals("__FIM__")) {
                    saida.println("Conexao encerrada pelo cliente.");
                    break;
                }

                saida.println(resposta); // Envia ao cliente
            }

        } catch (IOException e) {
            System.out.println("Erro na escrita: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                socket.close(); // Fecha o socket ao sair
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}