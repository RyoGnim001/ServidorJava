import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Servidor {
    private static final int PORTA = 12345;
    private static final int MAX_ACESSOS = 3;

    // Fila principal: controla quantas conexões podem estar "aguardando processamento"
    private final BlockingQueue<Conexao> filaConexoes = new ArrayBlockingQueue<>(MAX_ACESSOS);

    // Estrutura para armazenar conexões ativas (em processamento)
    private final List<Conexao> conexoesAtivas = Collections.synchronizedList(new ArrayList<>());

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            System.out.println("Servidor ligado na porta " + PORTA);
            System.out.println("Máximo de conexões simultâneas: " + MAX_ACESSOS);

            // Thread consumidora: fica em loop tirando conexões da fila
            Thread consumidor = new Thread(this::processarFila);
            consumidor.setDaemon(true);
            consumidor.start();

            // Loop principal (produtor): aceita novas conexões
            while (!serverSocket.isClosed()) {
                System.out.println("Aguardando cliente...");
                Socket cliente = serverSocket.accept();
                System.out.println("Cliente conectado: " + cliente.getInetAddress());

                // Cria o "envelope" com o socket e coloca na fila
                // Se a fila estiver cheia (MAX_ACESSOS), este put() BLOQUEIA
                filaConexoes.put(new Conexao(cliente));
                System.out.println("Cliente na fila. Total aguardando: " + filaConexoes.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Servidor interrompido.");
        }
    }

    /**
     * Thread consumidora: tira conexões da fila e ativa as threads de leitura/escrita.
     */
    private void processarFila() {
        while (true) {
            try {
                // take() bloqueia até ter algo na fila
                Conexao conexao = filaConexoes.take();
                conexoesAtivas.add(conexao);
                System.out.println("Ativando conexão. Ativas: " + conexoesAtivas.size());

                // Handshake inicial: estas duas linhas entram na fila de
                // respostas ANTES das threads começarem. Assim que a
                // ThreadEscrever ligar e chamar filaRespostas.take(), elas
                // serão as duas primeiras mensagens enviadas ao cliente —
                // exatamente o que o Client.py espera com receber_linha()
                // duas vezes seguidas logo após conectar.
                int porta = conexao.cliente().getPort();
                conexao.filaRepostas().put("__ID__" + porta);
                conexao.filaRepostas().put("Bem-vindo ao servidor!");

                // Cria as duas threads dedicadas para esta conexão
                Thread threadLer = new Thread(new ThreadLer(conexao), "Leitura-" + porta);
                Thread threadEscrever = new Thread(new ThreadEscrever(conexao), "Escrita-" + porta);

                // Inicia as threads
                threadLer.start();
                threadEscrever.start();

                // Thread "vigia" que monitora o fim da conexão e a remove da lista de ativas
                new Thread(() -> {
                    try {
                        threadLer.join();
                        threadEscrever.join();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    conexoesAtivas.remove(conexao);
                    System.out.println("Conexão encerrada. Ativas: " + conexoesAtivas.size());
                }).start();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}