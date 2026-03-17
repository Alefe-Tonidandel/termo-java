import java.awt.Color;

public class RetroTheme implements Theme {
    @Override
    public Color getBackgroundColor(Jogo.LetterState state) {
        return switch (state) {
            case CORRETA -> new Color(139, 69, 19); // marrom
            case PRESENTE -> new Color(210, 180, 140); // tan
            case ERRO -> new Color(128, 128, 128); // cinza
            default -> new Color(245, 245, 220); // bege
        };
    }

    @Override
    public Color getTextColor(Jogo.LetterState state) {
        return Color.BLACK;
    }
}
