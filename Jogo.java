import javax.swing.JOptionPane;
import java.io.*;
import java.util.*;
import java.text.Normalizer;

public class Jogo {
    public enum LetterState {
        CORRETA,       // Letra na posição correta
        PRESENTE,      // Letra presente na palavra, mas posição errada
        ERRO,          // Letra não está na palavra
        NAO_TENTADO    // Ainda não foi tentada
    }

    private Estatisticas estatisticas;
    private String palavraSecreta; // Palavra original (com acentos)
    private String palavraSecretaNormalizada; // Palavra normalizada (sem acentos)
    private int tentativasRestantes;
    private String nomeJogador;
    private Map<Character, LetterState> estadoTeclado;
    private int tamanhoPalavra;
    private String palavraFixa;
    private List<String> tentativas;
    private boolean[] letrasReveladasPorDica;
    private boolean isDesafioDiario;
    
    public Jogo(String nomeJogador, int tamanhoPalavra, String palavraFixa, boolean isDesafioDiario) {
    this.nomeJogador = nomeJogador;
    this.tamanhoPalavra = tamanhoPalavra;
    this.palavraFixa = palavraFixa;
    this.isDesafioDiario = isDesafioDiario;
    this.estadoTeclado = new HashMap<>();
    this.tentativasRestantes = 6;
    // Carrega estatísticas persistentes
    this.estatisticas = Estatisticas.carregarEstatisticas();
    this.tentativas = new ArrayList<>();
    inicializarTeclado();
    
    this.letrasReveladasPorDica = new boolean[tamanhoPalavra];
    Arrays.fill(letrasReveladasPorDica, false);
    
    if (palavraFixa != null) {
        setPalavraSecreta(palavraFixa);
    } else {
        selecionarPalavraAleatoria(tamanhoPalavra);
    }
}
    
    private void setPalavraSecreta(String palavra) {
        this.palavraSecreta = palavra;
        this.palavraSecretaNormalizada = normalizarPalavra(palavra);
    }

    public String getEstatisticasDetalhadas() {
        return String.format("<html><center>"
                + "Jogos: %d<br>"
                + "Vitórias: %d (%.0f%%)<br>"
                + "Sequência Atual: %d<br>"
                + "Melhor Sequência: %d<br>"
                + "Nível: %s"
                + "</center></html>",
                estatisticas.getPartidasJogadas(),
                estatisticas.getVitorias(),
                estatisticas.getPorcentagemVitorias(),
                estatisticas.getStreak(),
                estatisticas.getMaxStreak(),
                getNivelJogador());
    }

    public String normalizarPalavra(String palavra) {
    return Normalizer.normalize(palavra, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toUpperCase()
        .replace('Ç', 'C')
        .replaceAll("[^A-Z]", "");
    }

    public String getNivelJogador() {
        int vitorias = estatisticas.getVitorias();
        if (vitorias < 5) return "Iniciante";
        if (vitorias < 10) return "Intermediário";
        if (vitorias < 20) return "Avançado";
        if (vitorias < 30) return "Especialista";
        return "Mestre do TERMO!";
    }

    private void inicializarTeclado() {
        for (char c = 'A'; c <= 'Z'; c++) {
            estadoTeclado.put(c, LetterState.NAO_TENTADO);
        }
    }

    private void selecionarPalavraAleatoria(int tamanho) {
        List<String> palavras = new ArrayList<>();
        String arquivo = tamanho >= 7 ? "palavras_grandes.txt" : "palavras.txt";
        
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (normalizarPalavra(linha).length() == tamanho) {
                    palavras.add(linha);
                }
            }
        } catch (IOException e) {
            // Palavras padrão como fallback
            String[] palavrasPadrao = {
                "SOL", "LUAR", "CASA", "AMOR", "FLOR", "PEDRA", 
                "GATO", "RIO", "MAR", "SINO", "PRAIA", "MONTE",
                "ESTRELA", "VIOLÃO", "PÁSSARO", "MONTANHA", "COMPUTADOR"
            };
            for (String palavra : palavrasPadrao) {
                if (normalizarPalavra(palavra).length() == tamanho) {
                    palavras.add(palavra);
                }
            }
        }
        
        if (palavras.isEmpty()) {
            // Fallback extremo: gera uma palavra com 'A's
            palavras.add("A".repeat(tamanho));
        }

        Random rand = new Random();
        setPalavraSecreta(palavras.get(rand.nextInt(palavras.size())));
        Arrays.fill(letrasReveladasPorDica, false);
    }

    public Map<Character, LetterState> getEstadoTeclado() {
        return estadoTeclado;
    }

    public int getTentativasRestantes() {
        return tentativasRestantes;
    }

    public String getPalavraSecreta() {
        return palavraSecreta;
    }

    public static class GuessResult {
        private final LetterState[] estados;
        private final boolean acertou;
        private final boolean palavraValida;

        public GuessResult(LetterState[] estados, boolean acertou, boolean palavraValida) {
            this.estados = estados;
            this.acertou = acertou;
            this.palavraValida = palavraValida;
        }

        public boolean isAcertou() {
            return acertou;
        }
        
        public boolean isPalavraValida() {
            return palavraValida;
        }

        public LetterState getEstadoLetra(int posicao) {
            if (posicao >= 0 && posicao < estados.length) {
                return estados[posicao];
            }
            return LetterState.ERRO;
        }
        
        public LetterState[] getEstados() {
            return estados;
        }
    }

    public GuessResult verificarPalavra(String tentativa) {
    String tentativaNormalizada = normalizarPalavra(tentativa);
    
    // Verificar se a palavra é válida
    boolean palavraValida = ValidadorPalavras.isPalavraValida(tentativaNormalizada);
    
    if (!palavraValida) {
        return new GuessResult(new LetterState[0], false, false);
    }
        
    boolean acertou = tentativaNormalizada.equals(palavraSecretaNormalizada);
    LetterState[] estados = new LetterState[tentativaNormalizada.length()];
    
    if (acertou) {
        Arrays.fill(estados, LetterState.CORRETA);
        // Atualizar estado das letras no teclado
        for (char c : tentativaNormalizada.toCharArray()) {
            estadoTeclado.put(c, LetterState.CORRETA);
        }
        estatisticas.registrarVitoria(tentativas.size() + 1);
        
        // Se é um desafio diário, marca como completo
        if (isDesafioDiario) {
            estatisticas.registrarDesafioCompleto();
        }
    } else {
        char[] palavraArray = palavraSecretaNormalizada.toCharArray();
        char[] tentativaArray = tentativaNormalizada.toCharArray();
        Arrays.fill(estados, LetterState.ERRO);
        
        // Primeiro: marcar as corretas
        for (int i = 0; i < tentativaArray.length; i++) {
            if (tentativaArray[i] == palavraArray[i]) {
                estados[i] = LetterState.CORRETA;
                palavraArray[i] = 0; // Marcador de letra já usada
            }
        }
        
        // Segundo: marcar as presentes (não corretas)
        for (int i = 0; i < tentativaArray.length; i++) {
            if (estados[i] == LetterState.CORRETA) continue;
            
            for (int j = 0; j < palavraArray.length; j++) {
                if (palavraArray[j] != 0 && tentativaArray[i] == palavraArray[j]) {
                    estados[i] = LetterState.PRESENTE;
                    palavraArray[j] = 0; // Marcador de letra já usada
                    break;
                }
            }
        }
        
        // Atualizar estado das letras no teclado
        for (int i = 0; i < tentativaArray.length; i++) {
            char c = tentativaArray[i];
            LetterState novoEstado = estados[i];
            LetterState estadoAtual = estadoTeclado.getOrDefault(c, LetterState.NAO_TENTADO);
            
            // Hierarquia de estados: CORRETA > PRESENTE > ERRO
            if (novoEstado == LetterState.CORRETA || 
                (novoEstado == LetterState.PRESENTE && estadoAtual != LetterState.CORRETA) ||
                (novoEstado == LetterState.ERRO && estadoAtual == LetterState.NAO_TENTADO)) {
                estadoTeclado.put(c, novoEstado);
            }
        }
        
        // Se foi a última tentativa e não acertou, registrar derrota
        if (tentativasRestantes == 1) {
            estatisticas.registrarDerrota();
            
            // Se é um desafio diário, marca como completo mesmo ao perder
            if (isDesafioDiario) {
                estatisticas.registrarDesafioCompleto();
            }
        }
    }
    
    tentativas.add(tentativa);
    tentativasRestantes--;
    
    return new GuessResult(estados, acertou, palavraValida);
}      
           
    public void novoJogo() {
        tentativas.clear();
        tentativasRestantes = 6;
        estadoTeclado.clear();
        inicializarTeclado();
        Arrays.fill(letrasReveladasPorDica, false);
        
        if (palavraFixa == null) {
            selecionarPalavraAleatoria(tamanhoPalavra);
        } else {
            setPalavraSecreta(palavraFixa);
        }
    }
    
    public String getEstatisticas() {
        return String.format(
            "Jogos: %d | Vitórias: %d | Sequência: %d | Melhor: %d",
            estatisticas.getPartidasJogadas(),
            estatisticas.getVitorias(),
            estatisticas.getStreak(),
            estatisticas.getMaxStreak()
        );
    }
    
    public boolean jogoFinalizado() {
        return tentativasRestantes <= 0 || 
               tentativas.stream().anyMatch(t -> normalizarPalavra(t).equals(palavraSecretaNormalizada));
    }
    
    public boolean jogoFinalizadoComVitoria() {
        return tentativas.stream().anyMatch(t -> normalizarPalavra(t).equals(palavraSecretaNormalizada));
    }

    public char getHintLetter() {
        for (int i = 0; i < palavraSecreta.length(); i++) {
            if (!letrasReveladasPorDica[i]) {
                letrasReveladasPorDica[i] = true;
                return palavraSecreta.charAt(i);
            }
        }
        return 0;
    }

    public void useHint() {
        if (getHintLetter() != 0) {
            tentativasRestantes--;
        }
    }

    public Estatisticas getEstatisticasObj() {
        return estatisticas;
    }

    public List<String> getTentativas() {
        return tentativas;
    }
    
    public void salvarEstatisticas() {
        estatisticas.salvar();
    }
}
