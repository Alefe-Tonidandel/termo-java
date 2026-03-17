import java.text.Normalizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class DicionarioOnline {
    public enum ValidationResult {
        VALID, NOT_FOUND, INVALID, API_ERROR
    }

    private static final Set<String> dicionarioLocal = new HashSet<>();

    static {
        carregarDicionarioLocal();
    }

    private static void carregarDicionarioLocal() {
        try (BufferedReader br = new BufferedReader(new FileReader("palavras.txt"))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                String palavra = linha.trim();
                // Adiciona a palavra original
                dicionarioLocal.add(palavra.toLowerCase());
                // Adiciona a versão normalizada (sem acentos)
                dicionarioLocal.add(normalizarPalavra(palavra).toLowerCase());
            }
            System.out.println("Dicionário local carregado: " + dicionarioLocal.size() + " palavras.");
        } catch (Exception e) {
            System.out.println("Erro ao carregar dicionário local: " + e.getMessage());
            // Adiciona palavras padrão como fallback
            String[] palavrasPadrao = {"gato", "casa", "carro", "jardim", "elefante", "flor", "computador"};
            for (String palavra : palavrasPadrao) {
                dicionarioLocal.add(palavra.toLowerCase());
                dicionarioLocal.add(normalizarPalavra(palavra).toLowerCase());
            }
        }
    }

    private static String normalizarPalavra(String palavra) {
        return Normalizer.normalize(palavra, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace("ç", "c")
            .replace("Ç", "C")
            .toLowerCase();
    }

    public static ValidationResult validarPalavraDetalhado(String palavra) {
        if (palavra == null || palavra.trim().isEmpty()) {
            return ValidationResult.INVALID;
        }

        String normalizada = normalizarPalavra(palavra);

        // Verifica no dicionário local primeiro
        if (dicionarioLocal.contains(normalizada.toLowerCase())) {
            return ValidationResult.VALID;
        }

        // Se não encontrou localmente, tenta a API
        try {
            String urlStr = "https://api.dicionario-aberto.net/word/" + normalizada;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                // Se a API retornou sucesso, a palavra existe
                return ValidationResult.VALID;
            } else if (responseCode == 404) {
                return ValidationResult.NOT_FOUND;
            } else {
                return ValidationResult.API_ERROR;
            }
        } catch (Exception e) {
            System.out.println("Erro na API externa: " + e.getMessage());
            return ValidationResult.API_ERROR;
        }
    }

    public static boolean isPalavraValida(String palavra) {
        ValidationResult resultado = validarPalavraDetalhado(palavra);
        return resultado == ValidationResult.VALID;
    }
}
