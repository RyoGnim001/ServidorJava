import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ThreadLer implements Runnable {
    private final Socket socket;
    private final BlockingQueue<String> filaRespostas;
    private ProcessadorComando processadorComando;

    public ThreadLer(Conexao conexao) {
        this.socket = conexao.cliente();
        this.filaRespostas = conexao.filaRepostas();
        this.processadorComando = new ProcessadorComando();
    }

    @Override
    public void run() {
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String mensagem;

            while ((mensagem = leitor.readLine()) != null) {
                System.out.println("Mesagem: " + mensagem);
                String[] partes = mensagem.split(";", 3);
                String comando = partes[0];

                if (comando.equalsIgnoreCase("0")) {
                    filaRespostas.put("__FIM__");
                    break;
                }

                String resposta = processadorComando.processarComando(partes);
                filaRespostas.put(resposta);
            }

        } catch (IOException e) {
            System.out.println("Erro na leitura: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
