import java.awt.Color;

public class LightTheme implements Theme {
    @Override
    public Color getBackgroundColor(Jogo.LetterState state) {
        return switch (state) {
            case CORRETA -> new Color(106, 170, 100);
            case PRESENTE -> new Color(201, 180, 88);
            case ERRO -> new Color(211, 214, 218);
            default -> Color.WHITE;
        };
    }

    @Override
    public Color getTextColor(Jogo.LetterState state) {
        return state == Jogo.LetterState.NAO_TENTADO ? Color.BLACK : Color.WHITE;
    }
}
