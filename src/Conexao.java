import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public record Conexao(
        Socket cliente,
        BlockingQueue<String> filaRepostas
) {
    public Conexao(Socket cliente) {
        this(cliente, new ArrayBlockingQueue<>(10));
    }
}