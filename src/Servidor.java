import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Servidor {
    private static final int PORTA = 12345;
    private static final int MAX_ACESSOS = 3;

    // Cada conexão atendida ocupa uma posição até seu encerramento.
    private final BlockingQueue<Conexao> filaConexoes = new ArrayBlockingQueue<>(MAX_ACESSOS);

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor ligado na porta " + PORTA);
            System.out.println("Máximo de conexões simultâneas: " + MAX_ACESSOS);

            while (!serverSocket.isClosed()) {
                System.out.println("Aguardando cliente...");
                Socket cliente = serverSocket.accept();
                Conexao conexao = new Conexao(cliente);

                try {
                    // Se todas as posições estiverem ocupadas, esta thread espera
                    // até uma conexão em atendimento ser removida da fila.
                    filaConexoes.put(conexao);
                } catch (InterruptedException e) {
                    fecharSocket(cliente);
                    Thread.currentThread().interrupt();
                    System.out.println("Servidor interrompido.");
                    break;
                }

                try {
                    iniciarAtendimento(conexao);
                    System.out.println("Cliente conectado: " + cliente.getInetAddress());
                    System.out.println("Conexões em atendimento: " + filaConexoes.size());
                } catch (RuntimeException e) {
                    filaConexoes.remove(conexao);
                    fecharSocket(cliente);
                    throw e;
                }
            }
        }
    }

    private void iniciarAtendimento(Conexao conexao) {
        Socket cliente = conexao.cliente();
        int porta = cliente.getPort();

        // Mantém as duas mensagens iniciais antes das respostas aos comandos.
        conexao.filaRepostas().add("__ID__" + porta);
        conexao.filaRepostas().add("Bem-vindo ao servidor!");

        Thread threadLer = new Thread(new ThreadLer(conexao), "Leitura-" + porta);
        Thread threadEscrever = new Thread(new ThreadEscrever(conexao), "Escrita-" + porta);

        threadLer.start();
        threadEscrever.start();

        Thread monitor = new Thread(() -> {
            boolean interrompido = false;
            try {
                while (threadLer.isAlive() || threadEscrever.isAlive()) {
                    try {
                        threadLer.join();
                        threadEscrever.join();
                    } catch (InterruptedException e) {
                        interrompido = true;
                        fecharSocket(cliente);
                        threadLer.interrupt();
                        threadEscrever.interrupt();
                    }
                }
            } finally {
                fecharSocket(cliente);
                filaConexoes.remove(conexao);
                System.out.println("Conexão encerrada. Conexões em atendimento: " + filaConexoes.size());
                if (interrompido) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Monitor-" + porta);
        monitor.start();
    }

    private static void fecharSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }
}
