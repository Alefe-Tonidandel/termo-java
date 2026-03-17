// src/Palavra.java
public class Palavra {
    private String conteudo;
    
    public Palavra(String conteudo) {
        this.conteudo = conteudo.toUpperCase();
    }
    
    // Método necessário para acessar o conteúdo da palavra
    public String getConteudo() {
        return conteudo;
    }
    
    // Método para obter uma letra específica
    public char getLetra(int indice) {
        if (indice >= 0 && indice < conteudo.length()) {
            return conteudo.charAt(indice);
        }
        return '\0';
    }
    
    // Método para obter o tamanho da palavra
    public int getTamanho() {
        return conteudo.length();
    }
    
    @Override
    public String toString() {
        return conteudo;
    }
}
