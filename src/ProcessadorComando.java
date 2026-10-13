import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ProcessadorComando {

    public String processarComando(String[] partes) {
        String comando = partes[0].trim();

        if (comando.equals("4")) {
            return processarImagem();
        }

        if (comando.equals("5")) {
            return processarAluno();
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

    /**
     * Serializa um objeto Aluno para bytes, codifica em Base64 e devolve
     * como uma única linha de texto. O cliente Java detecta o prefixo
     * "SERIALIZED;" e faz o caminho inverso.
     */
    private String processarAluno() {
        try {
            // Aqui você pode variar os dados (ex.: sortear, ler de um arquivo etc.)
            Aluno aluno = new Aluno("João da Silva", "2024001234", "Ciência da Computação");

            // Serializa para um buffer em memória — NÃO no socket
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(aluno);
            }

            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());
            return "SERIALIZED;" + base64;

        } catch (IOException e) {
            System.out.println("Erro ao serializar Aluno: " + e.getMessage());
            return "ERRO;Não foi possível serializar o aluno no servidor";
        }
    }
}