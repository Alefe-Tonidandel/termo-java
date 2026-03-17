import java.text.Normalizer;

public class ValidadorPalavras {
    private static final int TAMANHO_MINIMO = 3;
    private static final int TAMANHO_MAXIMO = 20;

    public static boolean isPalavraValida(String palavra) {
        return validarPalavraDetalhado(palavra) == DicionarioOnline.ValidationResult.VALID;
    }

    public static DicionarioOnline.ValidationResult validarPalavraDetalhado(String palavra) {
        if (palavra == null || palavra.isBlank()) {
            return DicionarioOnline.ValidationResult.INVALID;
        }

        String normalizada = normalizarPalavra(palavra);

        if (normalizada.length() < TAMANHO_MINIMO) {
            return DicionarioOnline.ValidationResult.INVALID;
        }

        if (normalizada.length() > TAMANHO_MAXIMO) {
            return DicionarioOnline.ValidationResult.INVALID;
        }

        if (!normalizada.matches("^[a-z]+$")) {
            return DicionarioOnline.ValidationResult.INVALID;
        }

        return DicionarioOnline.validarPalavraDetalhado(normalizada);
    }

    private static String normalizarPalavra(String palavra) {
        return Normalizer.normalize(palavra, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase()
            .replace("ç", "c")
            .replace("Ç", "C")
            .replaceAll("[^a-z]", "");
    }
}
