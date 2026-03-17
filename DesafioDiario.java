import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DesafioDiario {
    private static String palavraDoDia;
    private static LocalDate dataAtualizacao;

    public static String getPalavraDoDia(Estatisticas stats) {
        LocalDate hoje = LocalDate.now();
        
        // Reseta o desafio se for um novo dia
        stats.resetDesafioDiario();
        
        // Se já temos uma palavra para hoje e o jogador pode jogar
        if (palavraDoDia != null && dataAtualizacao != null && 
            dataAtualizacao.equals(hoje) && stats.podeJogarDesafio()) {
            return palavraDoDia;
        }
        
        // Se o jogador já completou o desafio hoje
        if (stats.desafioHojeCompleto()) {
            return null;
        }
        
        // Gera uma nova palavra do dia
        List<String> palavras = carregarPalavras(5);
        if (!palavras.isEmpty()) {
            // Usa a data como semente para garantir a mesma palavra para todos no mesmo dia
            long seed = hoje.toEpochDay();
            palavraDoDia = palavras.get(new Random(seed).nextInt(palavras.size()));
            dataAtualizacao = hoje;
            System.out.println("Nova palavra do dia: " + palavraDoDia);
        } else {
            palavraDoDia = "TERMO"; // Fallback
        }
        
        return palavraDoDia;
    }

    private static List<String> carregarPalavras(int tamanho) {
        List<String> palavrasFiltradas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("palavras.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String palavra = linha.trim();
                if (palavra.length() == tamanho) {
                    palavrasFiltradas.add(palavra);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar palavras: " + e.getMessage());
            // Fallback
            palavrasFiltradas.add("TERMO");
            palavrasFiltradas.add("JOGAR");
            palavrasFiltradas.add("PALAV");
            palavrasFiltradas.add("LETRA");
        }
        return palavrasFiltradas;
    }
}
