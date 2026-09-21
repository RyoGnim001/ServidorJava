import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ClientJava {
    private final String host;
    private final int porta;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter saida;

    public ClientJava(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }

    /**
     * Estabelece a conexão com o servidor.
     */
    public void conectar() {
        try {
            socket = new Socket(host, porta);
            entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            saida = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            System.out.println("O cliente se conectou ao servidor!");
        } catch (IOException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
        }
    }

    /**
     * Envia uma mensagem para o servidor (adiciona a quebra de linha
     * automaticamente, igual o Client.py fazia com "mensagem + \n").
     */
    public boolean enviarMensagem(String mensagem) {
        if (!estaConectado()) {
            System.out.println("Cliente não está conectado. Chame conectar() primeiro.");
            return false;
        }

        saida.println(mensagem);
        if (saida.checkError()) {
            System.out.println("Erro ao enviar: o servidor encerrou a conexão.");
            return false;
        }
        return true;
    }

    /**
     * Lê exatamente uma linha do servidor, bloqueando até ela chegar.
     * Equivalente ao receber_linha() do Client.py.
     */
    public String receberLinha() {
        if (!estaConectado()) return null;
        try {
            socket.setSoTimeout(0); // 0 = sem timeout, bloqueia indefinidamente
            return entrada.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Aguarda e recebe a resposta completa do servidor: lê a primeira
     * linha de forma bloqueante e depois tenta ler linhas adicionais
     * com um timeout curto, assumindo que a resposta terminou quando
     * o servidor "fica em silêncio" por esse tempo.
     * Equivalente ao receber_resposta() do Client.py, mas usando
     * Socket.setSoTimeout() em vez de select() (não existe select()
     * exposto dessa forma em Java puro).
     */
    public String receberResposta(int timeoutEntreLinhasMs) {
        if (!estaConectado()) return null;

        StringBuilder resposta = new StringBuilder();
        try {
            // 1. Primeira linha: bloqueante, sem timeout
            socket.setSoTimeout(0);
            String primeira = entrada.readLine();
            if (primeira == null) {
                System.out.println("O servidor encerrou a sessão.");
                return null;
            }
            resposta.append(primeira);

            // 2. Linhas adicionais: com timeout curto
            socket.setSoTimeout(timeoutEntreLinhasMs);
            while (true) {
                try {
                    String linha = entrada.readLine();
                    if (linha == null) break; // conexão fechada
                    resposta.append("\n").append(linha);
                } catch (SocketTimeoutException e) {
                    break; // servidor "ficou em silêncio": resposta terminou
                }
            }
        } catch (IOException e) {
            return resposta.length() > 0 ? resposta.toString() : null;
        } finally {
            try {
                socket.setSoTimeout(0);
            } catch (IOException ignored) {
            }
        }

        return resposta.toString();
    }

    /**
     * Sobrecarga com timeout padrão de 200ms, igual ao valor padrão
     * usado no Client.py.
     */
    public String receberResposta() {
        return receberResposta(200);
    }

    /**
     * Fecha a conexão com o servidor.
     */
    public void fechar() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        System.out.println("Conexão encerrada.");
    }

    /**
     * Verifica se o socket ainda é válido.
     */
    public boolean estaConectado() {
        return socket != null && !socket.isClosed();
    }
}