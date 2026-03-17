import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tile extends JPanel {
    private char letter;
    private Jogo.LetterState state;
    private boolean focused;
    private InterfaceGrafica game;
    private int row, col;
    private Theme theme;
    private JLabel letterLabel;  // Adicionado JLabel para exibir a letra

    public Tile(int row, int col, InterfaceGrafica game) {
        this.row = row;
        this.col = col;
        this.game = game;
        this.state = Jogo.LetterState.NAO_TENTADO;
        this.focused = false;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(60, 60));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setOpaque(true);
        
        // Label para exibir a letra
        letterLabel = new JLabel("", SwingConstants.CENTER);
        letterLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(letterLabel, BorderLayout.CENTER);
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Só permitir foco na linha atual
                if (row == game.getTentativaAtual()) {
                    game.focarTile(row, col);
                }
            }
        });
    }
    
    public void setLetter(char letter, Jogo.LetterState state) {
        this.letter = letter;
        this.state = state;
        if (letter == '\0') {
            letterLabel.setText("");
        } else {
            letterLabel.setText(String.valueOf(letter));
        }
        updateAppearance();
    }
    
    public char getLetter() {
        return letter;
    }
    
    public void setTheme(Theme theme) {
        this.theme = theme;
        updateAppearance();
    }
    
    public void focar() {
        focused = true;
        setBorder(BorderFactory.createLineBorder(Color.YELLOW, 3));
    }
    
    public void desfocar() {
        focused = false;
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        updateAppearance();
    }
    
    public void reset() {
        letter = '\0';
        state = Jogo.LetterState.NAO_TENTADO;
        letterLabel.setText("");
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        setBackground(theme.getBackgroundColor(state));
        setForeground(theme.getTextColor(state));
    }
    
    public void setState(Jogo.LetterState state) {
    this.state = state;
    updateAppearance();
    
    // Animação ao revelar o estado
    if (state != Jogo.LetterState.NAO_TENTADO) {
        Timer timer = new Timer(50, e -> {
            setBackground(theme.getBackgroundColor(state).brighter());
            Timer resetTimer = new Timer(100, e2 -> {
                setBackground(theme.getBackgroundColor(state));
            });
            resetTimer.setRepeats(false);
            resetTimer.start();
        });
        timer.setRepeats(false);
        timer.start();
    }
}

public void updateAppearance() {
    if (theme != null) {
        setBackground(theme.getBackgroundColor(state));
        letterLabel.setForeground(theme.getTextColor(state));
        
        // Efeito de animação
        if (state != Jogo.LetterState.NAO_TENTADO) {
            Timer timer = new Timer(50, e -> {
                setBackground(theme.getBackgroundColor(state).brighter());
                Timer resetTimer = new Timer(100, e2 -> {
                    setBackground(theme.getBackgroundColor(state));
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            });
            timer.setRepeats(false);
            timer.start();
        }
    }
}
}
