import java.awt.Color;

public class ColorfulTheme implements Theme {
    @Override
    public Color getBackgroundColor(Jogo.LetterState state) {
        return switch (state) {
            case CORRETA -> new Color(0, 200, 100);    // Verde vibrante
            case PRESENTE -> new Color(255, 200, 0);   // Amarelo ouro
            case ERRO -> new Color(255, 80, 80);       // Vermelho coral
            default -> new Color(180, 220, 255);       // Azul claro
        };
    }

    @Override
    public Color getTextColor(Jogo.LetterState state) {
        return state == Jogo.LetterState.NAO_TENTADO ? 
            new Color(50, 50, 100) : Color.WHITE;
    }
}
