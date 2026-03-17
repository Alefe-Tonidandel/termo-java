import java.awt.Color;

public class DarkTheme implements Theme {
    @Override
    public Color getBackgroundColor(Jogo.LetterState state) {
        return switch (state) {
            case CORRETA -> new Color(83, 141, 78);
            case PRESENTE -> new Color(181, 159, 59);
            case ERRO -> new Color(58, 58, 60);
            default -> new Color(18, 18, 30);
        };
    }

    @Override
    public Color getTextColor(Jogo.LetterState state) {
        return Color.WHITE;
    }
}
