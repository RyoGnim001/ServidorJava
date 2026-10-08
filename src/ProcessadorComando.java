import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ProcessadorComando {
    public String processarComando(String[] partes) {
        String comando = partes[0].trim();

        if (comando.equals("4")) {
            return processarImagem();
        }

        try {
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

    private String processarImagem() {
        Path caminho = Path.of("imagem", "universo.jpg");
        if (!Files.isRegularFile(caminho)) {
            caminho = Path.of("ServidorJava", "imagem", "universo.jpg");
        }

        try {
            byte[] imagem = Files.readAllBytes(caminho);
            String dadosBase64 = Base64.getEncoder().encodeToString(imagem);
            return "OK;" + dadosBase64;
        } catch (IOException e) {
            System.out.println("Erro ao ler a imagem do servidor: " + e.getMessage());
            return "ERRO;Não foi possível ler imagem/universo.jpg no servidor";
        }
    }
}
