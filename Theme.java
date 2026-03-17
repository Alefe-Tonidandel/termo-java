import java.awt.Color;
import javax.swing.border.Border;

public interface Theme {
    Color getBackgroundColor(Jogo.LetterState state);
    Color getTextColor(Jogo.LetterState state);
}
