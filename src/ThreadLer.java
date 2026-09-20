import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class ThreadLer implements Runnable {
    private final Socket socket;
    private final BlockingQueue<String> filaRespostas;

    public ThreadLer(Conexao conexao) {
        this.socket = conexao.cliente();
        this.filaRespostas = conexao.filaRepostas();
    }

    @Override
    public void run() {
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String mensagem;

            while ((mensagem = leitor.readLine()) != null) {
                System.out.println("Mesagem: " + mensagem);
                String[] partes = mensagem.split(";");
                String comando = partes[0];

                if (comando.equalsIgnoreCase("0")) {
                    filaRespostas.put("__FIM__");
                    break;
                }

                String resposta = processarComando(partes);
                filaRespostas.put(resposta);
            }

        } catch (IOException e) {
            System.out.println("Erro na leitura: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String processarComando(String[] partes) {
        try {
            String comando = partes[0];
            double num1 = Double.parseDouble(partes[1]);
            double num2 = Double.parseDouble(partes[2]);
            double resultado;

            switch (comando) {
                case "1" -> resultado = num1 + num2;
                case "2" -> resultado = num1 - num2;
                case "3" -> resultado = num1 * num2;
                default -> {
                    return "ERRO;Comando inválido";
                }
            }
            return "OK;" + resultado;
        } catch (Exception e) {
            return "ERRO;Formato inválido. Use: comando;num1;num2";
        }
    }
}