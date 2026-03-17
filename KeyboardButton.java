import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.FontMetrics;

public class KeyboardButton extends JButton {
    private Jogo.LetterState state;
    private boolean hovered = false;
    private Theme theme;

    public KeyboardButton(String text, Theme theme) {
        super(text);
        this.state = Jogo.LetterState.NAO_TENTADO;
        this.theme = theme;
        setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 14));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Color.WHITE);
        setPreferredSize(new Dimension(
            text.equals("ENTER") || text.equals("DEL") ? 80 : 50, 
            60
        ));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }
    
    public void setTheme(Theme theme) {
        this.theme = theme;
        repaint();
    }

    public void setState(Jogo.LetterState state) {
        this.state = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color bgColor;
        switch (state) {
            case CORRETA: 
                bgColor = theme.getBackgroundColor(Jogo.LetterState.CORRETA);
                break;
            case PRESENTE: 
                bgColor = theme.getBackgroundColor(Jogo.LetterState.PRESENTE);
                break;
            case ERRO:     
                bgColor = theme.getBackgroundColor(Jogo.LetterState.ERRO);
                break;
            default:       
                bgColor = theme.getBackgroundColor(Jogo.LetterState.NAO_TENTADO);
        }
        
        // Efeito hover
        if (hovered) {
            bgColor = bgColor.brighter();
        }
        
        // Gradiente
        GradientPaint gp = new GradientPaint(
            0, 0, bgColor.brighter(),
            0, getHeight(), bgColor.darker()
        );
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        
        // Borda
        g2.setColor(bgColor.darker().darker());
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
        
        // Texto
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(getText(), x, y);
    }
}
