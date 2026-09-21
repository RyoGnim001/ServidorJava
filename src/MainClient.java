import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;

public class MainClient {

    public static void main(String[] args) {
        ClientJava client = new ClientJava("127.0.0.1", 12345);
        client.conectar();

        if (!client.estaConectado()) {
            return;
        }

        // --- Handshake inicial (cada mensagem é uma linha única) ---
        String idLinha = client.receberLinha();
        if (idLinha != null && idLinha.startsWith("__ID__")) {
            String meuId = idLinha.replace("__ID__", "").trim();
            System.out.println("Você é o Cliente #" + meuId);
        }

        String boasVindas = client.receberLinha();
        if (boasVindas != null) {
            System.out.println("[Servidor] " + boasVindas);
        }

        exibirMenu();

        // --- Loop principal ---
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n> ");
            String entradaUsuario = scanner.nextLine().trim();

            if (entradaUsuario.equals("0")) {
                client.enviarMensagem("0");
                break;
            }

            if (entradaUsuario.isEmpty()) {
                continue;
            }

            String mensagem = prepararMensagem(entradaUsuario);
            if (mensagem == null) {
                // Erro de validação (arquivo não encontrado, formato
                // inválido etc.) — volta a pedir uma entrada, sem
                // gastar uma ida e volta com o servidor.
                continue;
            }

            if (client.enviarMensagem(mensagem)) {
                String resposta = client.receberResposta();
                if (resposta != null) {
                    System.out.println("[Servidor] " + resposta);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        client.fechar();
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n===== MENU =====");
        System.out.println("1 - Somar");
        System.out.println("2 - Subtrair");
        System.out.println("3 - Multiplicar");
        System.out.println("4 - Enviar Imagem");
        System.out.println("0 - Encerrar Conexão");
        System.out.println("Formato: opcao;valor1;valor2  (ex: 1;2;3)");
        System.out.println("Para imagem: 4;caminho/da/imagem.png");
    }

    /**
     * Recebe a linha digitada pelo usuário (ex: "1;2;3" ou "4;foto.png")
     * e devolve a mensagem pronta para envio ao servidor.
     *
     * Para as opções 1, 2 e 3, a linha já está no formato que o
     * servidor espera, então é enviada como veio (a lógica de somar/
     * subtrair/multiplicar fica por conta do servidor).
     *
     * Para a opção 4, o segundo campo é tratado como um caminho de
     * arquivo local: o conteúdo é lido, convertido para base64 (para
     * poder trafegar como texto numa única linha) e reempacotado como
     * "opcao;nome_do_arquivo;dados_em_base64".
     */
    private static String prepararMensagem(String entrada) {
        String[] partes = entrada.split(";");
        String opcao = partes[0].trim();

        if (opcao.equals("4")) {
            if (partes.length < 2 || partes[1].trim().isEmpty()) {
                System.out.println("Informe o caminho da imagem. Ex: 4;caminho/imagem.png");
                return null;
            }

            String caminho = partes[1].trim();
            Path arquivo = Path.of(caminho);

            if (!Files.isRegularFile(arquivo)) {
                System.out.println("Arquivo não encontrado: " + caminho);
                return null;
            }

            try {
                byte[] conteudo = Files.readAllBytes(arquivo);
                // Base64.getEncoder() (padrão) não insere quebras de
                // linha no resultado, então é seguro para um
                // protocolo baseado em linhas.
                String dadosB64 = Base64.getEncoder().encodeToString(conteudo);
                String nomeArquivo = arquivo.getFileName().toString();
                return "4;" + nomeArquivo + ";" + dadosB64;
            } catch (IOException e) {
                System.out.println("Erro ao ler o arquivo: " + e.getMessage());
                return null;
            }
        }

        // Opções 1, 2, 3 (e qualquer outra que o servidor saiba tratar)
        return entrada;
    }
}