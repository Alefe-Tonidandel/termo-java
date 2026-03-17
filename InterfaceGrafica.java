import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;

public class InterfaceGrafica extends JFrame {
    private JFrame frame;
    private JPanel gamePanel, keyboardPanel, headerPanel, footerPanel;
    private Tile[][] tiles;
    private int tentativaAtual, colunaAtual, tamanhoPalavra;
    private JLabel statsLabel, titleLabel;
    private Theme currentTheme;
    private Timer animationTimer;
    private float hueShift = 0;
    private Random random = new Random();
    private List<Point> particles = new ArrayList<>();
    private Jogo jogo;
    private JButton hintButton;
    private JButton themeBtn;
    private int dicasDisponiveis = 1;
    private Tile tileFocado; 
    private JButton botaoEnter;
    private JButton botaoDelete;
    private int linhaAtual = 0; 
    private JButton helpButton; 
    private KeyEventDispatcher keyDispatcher;
    private JButton muteBtn;
    private JButton fullscreenBtn;
    private boolean isDesafioDiario;
    
    public InterfaceGrafica(String nome, int tamanho, String fixa, String tema, boolean isDesafioDiario) {
        System.out.println("Iniciando InterfaceGrafica...");
        System.out.println("Nome: " + nome);
        System.out.println("Tamanho: " + tamanho);
        System.out.println("Palavra fixa: " + (fixa != null ? fixa : "null"));
        System.out.println("Tema: " + tema);
        System.out.println("Desafio diário: " + isDesafioDiario);
        
        this.tamanhoPalavra = tamanho;
        this.tentativaAtual = 0;
        this.colunaAtual = 0;
        this.linhaAtual = 0;
        this.isDesafioDiario = isDesafioDiario;
        this.jogo = new Jogo(nome, tamanho, fixa, isDesafioDiario);
        
        switch (tema) {
            case "Claro":
                this.currentTheme = new LightTheme();
                break;
            case "Colorido":
                this.currentTheme = new ColorfulTheme();
                break;
            case "Retrô":
                this.currentTheme = new RetroTheme();
                break;
            default:
                this.currentTheme = new DarkTheme();
        }
        
        buildUI();
        configurarCapturaTeclado();
        frame.setVisible(true);
        System.out.println("InterfaceGrafica iniciada com sucesso!");
        
        // Iniciar música de fundo
        SoundManager.startBackgroundMusic();
    }
    
    // Manter o construtor anterior para compatibilidade
    public InterfaceGrafica(String nome, int tamanho, String fixa, String tema) {
        this(nome, tamanho, fixa, tema, false);
    }
    
    private void configurarCapturaTeclado() {
        keyDispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    focarTileAtual();
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        botaoEnter.doClick();
                        return true;
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        botaoDelete.doClick();
                        return true;
                    } else if (Character.isLetter(e.getKeyChar())) {
                        char letra = Character.toUpperCase(e.getKeyChar());
                        onLetter(letra);
                        return true;
                    }
                }
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
    }
    
    private void buildUI() {
        frame = new JFrame("TERMO ULTRA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(720, 820);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(new Color(18, 18, 30));
        frame.setUndecorated(true);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), 40, 40));
            }
        });
        
        // Adicionar listener para fechar a janela
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SoundManager.stopAll();
            }
        });
        
        headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color color1 = Color.getHSBColor(hueShift, 0.8f, 0.4f);
                Color color2 = Color.getHSBColor(hueShift + 0.4f, 0.8f, 0.4f);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                if (particles.isEmpty() && getWidth() > 0 && getHeight() > 0) {
                    for (int i = 0; i < 30; i++) {
                        particles.add(new Point(
                            random.nextInt(getWidth()),
                            random.nextInt(getHeight())
                        ));
                    }
                }
                g2d.setColor(new Color(255, 255, 255, 120));
                for (Point p : particles) {
                    g2d.fillOval(p.x, p.y, 3, 3);
                }
            }
        };
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 70));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 215, 0)));
        
        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 10));
        windowControls.setOpaque(false);
        
        muteBtn = createControlButton("🔊", new Color(180, 180, 180));
        muteBtn.setToolTipText("Silenciar");
        muteBtn.addActionListener(e -> toggleMute());
        windowControls.add(muteBtn);
        
        themeBtn = new JButton("🌙");
        themeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        themeBtn.setForeground(Color.WHITE);
        themeBtn.setContentAreaFilled(false);
        themeBtn.setBorderPainted(false);
        themeBtn.addActionListener(e -> toggleTheme());
        
        fullscreenBtn = createControlButton("⛶", new Color(100, 100, 255));
        fullscreenBtn.addActionListener(e -> toggleFullScreen());
        fullscreenBtn.setToolTipText("Alternar tela cheia");
        
        JButton minimizeBtn = createControlButton("−", new Color(255, 204, 0));
        minimizeBtn.setToolTipText("Minimizar");
        minimizeBtn.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        
        JButton closeBtn = createControlButton("×", new Color(255, 80, 80));
        closeBtn.setToolTipText("Fechar jogo");
        closeBtn.addActionListener(e -> {
            SoundManager.stopAll();
            System.exit(0);
        });
        
        JButton menuBtn = createControlButton("≡", new Color(100, 200, 100));
        menuBtn.setToolTipText("Menu de opções");
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mudarTemaItem = new JMenuItem("Mudar Tema");
        JMenuItem voltarMenuItem = new JMenuItem("Voltar ao Menu");
        JMenuItem sairMenuItem = new JMenuItem("Sair");
        
        mudarTemaItem.addActionListener(e -> toggleTheme());
        voltarMenuItem.addActionListener(e -> voltarAoMenu());
        sairMenuItem.addActionListener(e -> {
            SoundManager.stopAll();
            System.exit(0);
        });
        
        popupMenu.add(mudarTemaItem);
        popupMenu.add(voltarMenuItem);
        popupMenu.add(sairMenuItem);
        
        menuBtn.addActionListener(e -> popupMenu.show(menuBtn, 0, menuBtn.getHeight()));
        
        windowControls.add(menuBtn);
        windowControls.add(themeBtn);
        windowControls.add(fullscreenBtn);
        windowControls.add(minimizeBtn);
        windowControls.add(closeBtn);
        
        headerPanel.add(windowControls, BorderLayout.EAST);
        
        titleLabel = new JLabel("TERMO ULTRA");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new GlowBorder(15, new Color(0, 200, 255, 100)));
        
        JPanel dragPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        dragPanel.setOpaque(false);
        dragPanel.setPreferredSize(new Dimension(frame.getWidth(), 70));
        dragPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        
        dragPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                frame.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }
            public void mouseReleased(MouseEvent e) {
                frame.setCursor(Cursor.getDefaultCursor());
            }
        });
        
        dragPanel.addMouseMotionListener(new MouseMotionAdapter() {
            private Point mouseDownCompCoords = null;
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                frame.setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }
        });
        
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        titleContainer.add(titleLabel, BorderLayout.CENTER);
        titleContainer.setBorder(new EmptyBorder(10, 0, 10, 0));
        titleContainer.add(dragPanel, BorderLayout.CENTER);
        
        headerPanel.add(titleContainer, BorderLayout.CENTER);
        frame.add(headerPanel, BorderLayout.NORTH);
        
        footerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 30));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2d.setColor(new Color(200, 230, 255, 80));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2d.dispose();
            }
        };
        footerPanel.setLayout(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        statsLabel = new JLabel(jogo.getEstatisticasDetalhadas());
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(240, 240, 255));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        footerPanel.add(statsLabel, BorderLayout.CENTER);
        frame.add(footerPanel, BorderLayout.SOUTH);
        
        gamePanel = new JPanel(new GridLayout(6, tamanhoPalavra, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 40), 
                              getWidth(), getHeight(), new Color(35, 35, 55));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 255, 255, 10));
                for (int i = 0; i <= getWidth(); i += 40) {
                    g2d.drawLine(i, 0, i, getHeight());
                }
                for (int i = 0; i <= getHeight(); i += 40) {
                    g2d.drawLine(0, i, getWidth(), i);
                }
            }
        };
        gamePanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        gamePanel.setOpaque(false);
        
        tiles = new Tile[6][tamanhoPalavra];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < tamanhoPalavra; j++) {
                tiles[i][j] = new Tile(i, j, this);
                tiles[i][j].setTheme(currentTheme);
                gamePanel.add(tiles[i][j]);
            }
        }
        frame.add(gamePanel, BorderLayout.CENTER);
        
        keyboardPanel = new JPanel();
        keyboardPanel.setLayout(new GridLayout(4, 1, 8, 8));
        keyboardPanel.setBorder(new EmptyBorder(15, 20, 25, 20));
        keyboardPanel.setOpaque(false);
        
        String[] rows = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};
        for (String row : rows) {
            JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
            rowPanel.setOpaque(false);
            for (char c : row.toCharArray()) {
                JButton btn = createKeyboardButton(c);
                rowPanel.add(btn);
            }
            keyboardPanel.add(rowPanel);
        }
        
        JPanel controlRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        controlRow.setOpaque(false);
        
        botaoEnter = createSpecialButton("ENVIAR", new Color(46, 204, 113));
        botaoEnter.setToolTipText("Submeter tentativa");
        
        botaoDelete = createSpecialButton("⌫", new Color(231, 76, 60));
        botaoDelete.setToolTipText("Apagar letra");
        
        JButton newGameBtn = createSpecialButton("NOVO", new Color(52, 152, 219));
        newGameBtn.setToolTipText("Reiniciar jogo");
        
        hintButton = createSpecialButton("DICA (" + dicasDisponiveis + ")", new Color(155, 89, 182));
        hintButton.setToolTipText("Revelar uma letra correta (restam " + dicasDisponiveis + ")");
        hintButton.addActionListener(e -> showHint());
        
        helpButton = createSpecialButton("AJUDA", new Color(100, 100, 255));
        helpButton.setToolTipText("Mostrar legenda das cores");
        helpButton.addActionListener(e -> mostrarLegendaCores());
        
        botaoEnter.addActionListener(e -> {
            focarTileAtual();
            processar();
        });
        
        botaoDelete.addActionListener(e -> {
            focarTileAtual();
            apagar();
        });
        
        newGameBtn.addActionListener(e -> resetGame());
        
        controlRow.add(botaoEnter);
        controlRow.add(botaoDelete);
        controlRow.add(newGameBtn);
        controlRow.add(hintButton);
        controlRow.add(helpButton);
        
        keyboardPanel.add(controlRow);
        frame.add(keyboardPanel, BorderLayout.SOUTH);
        
        frame.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                focarTileAtual();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    botaoEnter.doClick();
                } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    botaoDelete.doClick();
                } else if (Character.isLetter(e.getKeyChar())) {
                    char letra = Character.toUpperCase(e.getKeyChar());
                    onLetter(letra);
                }
            }
        });
        
        animationTimer = new Timer(30, e -> {
            hueShift = (hueShift + 0.005f) % 1.0f;
            headerPanel.repaint();
        });
        animationTimer.start();
        
        frame.setVisible(true);
        frame.requestFocusInWindow(); 
        frame.setFocusable(true);
        
        frame.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                frame.requestFocusInWindow();
                focarTileAtual();
            }
        });
        
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                frame.requestFocusInWindow();
                focarTileAtual();
            }
        });
        
        muteBtn.setText(SoundManager.isMuted() ? "🔇" : "🔊");
        muteBtn.setToolTipText(SoundManager.isMuted() ? "Ativar som" : "Silenciar");
        
        // Focar o primeiro tile ao iniciar
        SwingUtilities.invokeLater(() -> focarTile(0, 0));
    }
    
    private void focarTileAtual() {
        focarTile(linhaAtual, colunaAtual);
    }
    
    private void toggleMute() {
        SoundManager.toggleMute();
        muteBtn.setText(SoundManager.isMuted() ? "🔇" : "🔊");
        muteBtn.setToolTipText(SoundManager.isMuted() ? "Ativar som" : "Silenciar");
    }
    
    private void mostrarLegendaCores() {
        String mensagem = "<html><div style='text-align: center;'>"
            + "<h2>Legenda de Cores</h2>"
            + "<p>As cores indicam:</p>"
            + "<table align='center' cellpadding='5'>"
            + "<tr><td bgcolor='" + colorToHex(currentTheme.getBackgroundColor(Jogo.LetterState.CORRETA)) + "' width='30'>&nbsp;</td><td>Letra na posição correta</td></tr>"
            + "<tr><td bgcolor='" + colorToHex(currentTheme.getBackgroundColor(Jogo.LetterState.PRESENTE)) + "' width='30'>&nbsp;</td><td>Letra correta, posição errada</td></tr>"
            + "<tr><td bgcolor='" + colorToHex(currentTheme.getBackgroundColor(Jogo.LetterState.ERRO)) + "' width='30'>&nbsp;</td><td>Letra não existe</td></tr>"
            + "</table></div></html>";
        JOptionPane.showMessageDialog(frame, mensagem, "Legenda de Cores", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private void mostrarFeedbackInvalido(String mensagem) {
        JOptionPane.showMessageDialog(frame, mensagem, "Palavra Inválida", JOptionPane.WARNING_MESSAGE);
    }
    
    private void toggleFullScreen() {
        if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            frame.setExtendedState(JFrame.NORMAL);
            frame.setSize(720, 820);
            frame.setLocationRelativeTo(null);
            fullscreenBtn.setText("⛶");
        } else {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            fullscreenBtn.setText("❐");
        }
    }
    
    private void toggleTheme() {
        if (currentTheme instanceof DarkTheme) {
            currentTheme = new LightTheme();
        } else if (currentTheme instanceof LightTheme) {
            currentTheme = new ColorfulTheme();
        } else if (currentTheme instanceof ColorfulTheme) {
            currentTheme = new RetroTheme();
        } else {
            currentTheme = new DarkTheme();
        }
        applyTheme();
        updateThemeButtonIcon();
    }
    
    private void updateThemeButtonIcon() {
        if (currentTheme instanceof DarkTheme) {
            themeBtn.setText("🌙"); 
        } else if (currentTheme instanceof LightTheme) {
            themeBtn.setText("☀️"); 
        } else if (currentTheme instanceof ColorfulTheme) {
            themeBtn.setText("🌈"); 
        } else {
            themeBtn.setText("🕹️"); 
        }
    }
    
    private void applyTheme() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < tamanhoPalavra; j++) {
                tiles[i][j].setTheme(currentTheme);
                tiles[i][j].updateAppearance();
            }
        }
        if (currentTheme instanceof DarkTheme) {
            frame.getContentPane().setBackground(new Color(18, 18, 30));
        } else if (currentTheme instanceof LightTheme) {
            frame.getContentPane().setBackground(new Color(230, 230, 240));
        } else if (currentTheme instanceof ColorfulTheme) {
            frame.getContentPane().setBackground(new Color(200, 220, 255));
        } else if (currentTheme instanceof RetroTheme) {
            frame.getContentPane().setBackground(new Color(245, 245, 220));
        }
        atualizarTecladoVirtual();
        headerPanel.repaint();
        footerPanel.repaint();
    }
    
    private void updateKeyboardTheme() {
        for (Component comp : keyboardPanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component btn : ((JPanel) comp).getComponents()) {
                    if (btn instanceof JButton) {
                        JButton button = (JButton) btn;
                        String text = button.getText();
                        if (text.length() == 1) {
                            char c = text.charAt(0);
                            Jogo.LetterState state = jogo.getEstadoTeclado().get(c);
                            if (state != null) {
                                button.setBackground(currentTheme.getBackgroundColor(state));
                                button.setForeground(currentTheme.getTextColor(state));
                            } else {
                                button.setBackground(currentTheme.getBackgroundColor(Jogo.LetterState.NAO_TENTADO));
                                button.setForeground(currentTheme.getTextColor(Jogo.LetterState.NAO_TENTADO));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private JButton createControlButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(30, 30));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
                btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
            }
        });
        return btn;
    }
    
    private JButton createKeyboardButton(char c) {
        JButton btn = new JButton(String.valueOf(c));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(new Color(50, 50, 80));
        btn.setForeground(new Color(220, 220, 255));
        btn.setBorder(new RoundedBorder(10, new Color(80, 80, 120)));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(70, 70, 120));
                btn.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 80));
                btn.setForeground(new Color(220, 220, 255));
            }
        });
        btn.addActionListener(e -> {
            onLetter(c);
            frame.requestFocusInWindow();
        });
        return btn;
    }
    
    private JButton createSpecialButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setBorder(new CompoundBorder(
            new LineBorder(color.darker(), 2),
            new EmptyBorder(6, 15, 6, 15)
        ));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color);
            }
        });
        return btn;
    }
    
    private void onLetter(char c) {
        focarTileAtual();
        SoundManager.playKeyPress();
        if (tileFocado != null) {
            tileFocado.setLetter(Character.toUpperCase(c), Jogo.LetterState.NAO_TENTADO);
            int nextCol = colunaAtual + 1;
            if (nextCol < tamanhoPalavra) {
                focarTile(linhaAtual, nextCol);
            } else {
                tileFocado.desfocar();
                tileFocado = null;
                colunaAtual = tamanhoPalavra; // Marcar como completo
            }
        } else if (colunaAtual < tamanhoPalavra) {
            tiles[tentativaAtual][colunaAtual].setLetter(Character.toUpperCase(c), Jogo.LetterState.NAO_TENTADO);
            colunaAtual++;
            focarTile(tentativaAtual, colunaAtual);
        }
    }
    
    private void apagar() {
        focarTileAtual();
        SoundManager.playKeyPress();
        if (tileFocado != null) {
            tileFocado.setLetter('\0', Jogo.LetterState.NAO_TENTADO);
            if (colunaAtual > 0) {
                focarTile(linhaAtual, colunaAtual - 1);
            }
        } else if (colunaAtual > 0) {
            colunaAtual--;
            tiles[tentativaAtual][colunaAtual].setLetter('\0', Jogo.LetterState.NAO_TENTADO);
            focarTile(tentativaAtual, colunaAtual);
        }
    }
    
    private void processar() {
        if (tentativaAtual < 0 || tentativaAtual >= 6) return;
        if (colunaAtual == tamanhoPalavra) {
            StringBuilder palavra = new StringBuilder();
            for (int i = 0; i < tamanhoPalavra; i++) {
                palavra.append(tiles[tentativaAtual][i].getLetter());
            }
            String palavraNormalizada = normalizarPalavra(palavra.toString());
            
            if (palavraNormalizada.length() != tamanhoPalavra) {
                JOptionPane.showMessageDialog(frame, "Palavra incompleta!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Verificar se a palavra é válida
            DicionarioOnline.ValidationResult resultadoValidacao = ValidadorPalavras.validarPalavraDetalhado(palavraNormalizada);

            if (resultadoValidacao != DicionarioOnline.ValidationResult.VALID) {
                SoundManager.playWrong();
                animarLinhaInvalida();
                
                switch (resultadoValidacao) {
                    case NOT_FOUND:
                        mostrarFeedbackInvalido("Palavra não existe em português!");
                        break;
                    case INVALID:
                        // Verificar se é por tamanho
                        if (palavraNormalizada.length() < 3) {
                            mostrarFeedbackInvalido("Palavra muito curta! Mínimo 3 letras.");
                        } else {
                            mostrarFeedbackInvalido("Palavra inválida!");
                        }
                        break;
                    case API_ERROR:
                        mostrarFeedbackInvalido("Erro de conexão. Verifique sua internet.");
                        break;
                    default:
                        mostrarFeedbackInvalido("Palavra inválida!");
                }
                return;
            }
            
            Jogo.GuessResult result = jogo.verificarPalavra(palavraNormalizada);
            
            for (int i = 0; i < tamanhoPalavra; i++) {
                tiles[tentativaAtual][i].setState(result.getEstadoLetra(i));
            }
            statsLabel.setText(jogo.getEstatisticasDetalhadas());
            
            if (result.isAcertou()) {
                SoundManager.playWin();
                animarVitoria();
                verificarConquistas();
                
                // Se for desafio diário, marcar como completo
                if (isDesafioDiario) {
                    jogo.getEstatisticasObj().registrarDesafioCompleto();
                }
                
                Timer fimTimer = new Timer(2000, e -> {
                    mostrarEstatisticas();
                    resetGame();
                });
                fimTimer.setRepeats(false);
                fimTimer.start();
            } else if (jogo.getTentativasRestantes() <= 0) {
                SoundManager.playLose();
                
                // Se for desafio diário, marcar como completo mesmo ao perder
                if (isDesafioDiario) {
                    jogo.getEstatisticasObj().registrarDesafioCompleto();
                }
                
                Timer fimTimer = new Timer(2000, e -> {
                    mostrarEstatisticas();
                    resetGame();
                });
                fimTimer.setRepeats(false);
                fimTimer.start();
            } else {
                SoundManager.playCorrect();
                tentativaAtual++;
                colunaAtual = 0;
                focarTile(tentativaAtual, 0);
            }
            atualizarTecladoVirtual();
        }
    }

    private String normalizarPalavra(String palavra) {
        return Normalizer.normalize(palavra, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toUpperCase()
            .replace('Ç', 'C')
            .replaceAll("[^A-Z]", "");
    }
    
    private boolean validarPalavra(String palavra) {
        if (palavra.length() != tamanhoPalavra) {
            JOptionPane.showMessageDialog(frame, 
                "A palavra deve ter " + tamanhoPalavra + " letras!", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!palavra.matches("[A-Z]+")) {
            JOptionPane.showMessageDialog(frame, 
                "Apenas letras são permitidas!", 
                "Erro", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    private void animarLinhaInvalida() {
        for (int i = 0; i < tamanhoPalavra; i++) {
            final Tile tile = tiles[tentativaAtual][i];
            Timer animTimer = new Timer(100 * i, e -> {
                tile.setBackground(new Color(255, 100, 100));
                Timer resetTimer = new Timer(200, e2 -> {
                    tile.setBackground(currentTheme.getBackgroundColor(Jogo.LetterState.NAO_TENTADO));
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            });
            animTimer.setRepeats(false);
            animTimer.start();
        }
    }
    
    private void resetGame() {
        SoundManager.stopBackgroundMusic();
        tentativaAtual = 0;
        colunaAtual = 0;
        linhaAtual = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < tamanhoPalavra; j++) {
                tiles[i][j].reset();
            }
        }
        jogo.novoJogo();
        statsLabel.setText(jogo.getEstatisticasDetalhadas());
        atualizarTecladoVirtual();
        dicasDisponiveis = 1;
        hintButton.setText("DICA (" + dicasDisponiveis + ")");
        hintButton.setToolTipText("Revelar uma letra correta (restam " + dicasDisponiveis + ")");
        hintButton.setEnabled(true);
        if (tileFocado != null) {
            tileFocado.desfocar();
            tileFocado = null;
        }
        SwingUtilities.invokeLater(() -> {
            focarTile(0, 0);
            frame.requestFocusInWindow();
            gamePanel.requestFocusInWindow();
            botaoEnter.setEnabled(true);
            botaoDelete.setEnabled(true);
        });
        if (!SoundManager.isMuted()) {
            SoundManager.startBackgroundMusic();
        }
    }
    
    public void focarTile(int row, int col) {
        if (row >= 0 && row < 6 && col >= 0 && col < tamanhoPalavra) {
            if (tileFocado != null) {
                tileFocado.desfocar();
            }
            tileFocado = tiles[row][col];
            tileFocado.focar();
            colunaAtual = col;
            linhaAtual = row;
            tentativaAtual = row;
            tileFocado.requestFocusInWindow();
        }
    }
    
    static class GlowBorder extends AbstractBorder {
        private int thickness;
        private Color glowColor;
        public GlowBorder(int thickness, Color glowColor) {
            this.thickness = thickness;
            this.glowColor = glowColor;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 0; i < thickness; i++) {
                float alpha = (thickness - i) / (float) thickness * 0.5f;
                g2d.setColor(new Color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), (int) (255 * alpha)));
                g2d.drawRoundRect(x + i, y + i, width - 1 - 2*i, height - 1 - 2*i, 20, 20);
            }
            g2d.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }
    }
    
    static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius+1, this.radius+1, this.radius+1, this.radius+1);
        }
        public boolean isBorderOpaque() {
            return true;
        }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
            g2d.dispose();
        }
    }
    
    private void voltarAoMenu() {
        int opcao = JOptionPane.showConfirmDialog(
            frame,
            "Deseja voltar ao menu principal?\nSeu progresso atual será perdido.",
            "Voltar ao Menu",
            JOptionPane.YES_NO_OPTION
        );
        if (opcao == JOptionPane.YES_OPTION) {
            SoundManager.stopAll();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
            frame.dispose();
            animationTimer.stop();
            new TelaInicial();
        }
    }
    
    private void showHint() {
        if (dicasDisponiveis <= 0) {
            JOptionPane.showMessageDialog(frame, 
                "Você já usou todas as dicas disponíveis nesta partida", 
                "Dicas Esgotadas", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String mensagem = "<html><div style='text-align: center; width: 300px;'>"
            + "<h3>Usar Dica</h3>"
            + "<p>Usar uma dica revelará a <b>primeira letra correta</b> ainda não descoberta.</p>"
            + "<p>Isso custará 1 tentativa.</p>"
            + "<p style='color: #888; font-size: 12px;'>"
            + "Dica: A dica sempre revela a primeira letra da palavra que ainda não foi descoberta"
            + "</p></div></html>";
        
        int opcao = JOptionPane.showConfirmDialog(
            frame,
            mensagem,
            "Usar Dica",
            JOptionPane.YES_NO_OPTION
        );
        
        if (opcao == JOptionPane.YES_OPTION) {
            char hintLetter = jogo.getHintLetter();
            if (hintLetter != 0) {
                for (int i = 0; i < tamanhoPalavra; i++) {
                    if (jogo.getPalavraSecreta().charAt(i) == hintLetter) {
                        tiles[tentativaAtual][i].setLetter(hintLetter, Jogo.LetterState.CORRETA);
                        if (tileFocado != null) {
                            tileFocado.desfocar();
                            tileFocado = null;
                        }
                        colunaAtual = Math.max(colunaAtual, i+1);
                        focarTile(tentativaAtual, colunaAtual);
                        break;
                    }
                }
                jogo.useHint();
                statsLabel.setText(jogo.getEstatisticasDetalhadas());
            }
        }
        dicasDisponiveis--;
        hintButton.setText("DICA (" + dicasDisponiveis + ")");
        hintButton.setToolTipText("Revelar uma letra correta (restam " + dicasDisponiveis + ")");
        if (dicasDisponiveis <= 0) {
            hintButton.setEnabled(false);
        }
    }
    
    private void atualizarTecladoVirtual() {
        Map<Character, Jogo.LetterState> estadoTeclado = jogo.getEstadoTeclado();
        for (Component rowComp : keyboardPanel.getComponents()) {
            if (rowComp instanceof JPanel) {
                JPanel rowPanel = (JPanel) rowComp;
                for (Component btnComp : rowPanel.getComponents()) {
                    if (btnComp instanceof JButton) {
                        JButton btn = (JButton) btnComp;
                        if (btn.getText().length() == 1) {
                            char letra = btn.getText().charAt(0);
                            Jogo.LetterState estado = estadoTeclado.get(letra);
                            if (estado != null) {
                                btn.setBackground(currentTheme.getBackgroundColor(estado));
                                btn.setForeground(currentTheme.getTextColor(estado));
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Método para adicionar estatísticas ao painel
    private void addStat(JPanel panel, String label, String value) {
        JPanel statPanel = new JPanel(new BorderLayout(10, 0));
        statPanel.setOpaque(false);
        
        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        labelLbl.setForeground(new Color(180, 180, 255));
        
        JLabel valueLbl = new JLabel(value);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLbl.setForeground(Color.WHITE);
        valueLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statPanel.add(labelLbl, BorderLayout.WEST);
        statPanel.add(valueLbl, BorderLayout.CENTER);
        panel.add(statPanel);
    }
    
    // Classe para o gráfico de distribuição
    class DistributionChartPanel extends JPanel {
        private Estatisticas estatisticas;
        
        public DistributionChartPanel(Estatisticas estatisticas) {
            this.estatisticas = estatisticas;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            
            // Título do gráfico
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String title = "Distribuição de Vitórias";
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            g2d.drawString(title, (width - titleWidth) / 2, 20);
            
            // Valores
            int maxValue = estatisticas.getMaxBarValue();
            if (maxValue == 0) maxValue = 1;
            int barWidth = 40;
            int spacing = 20;
            int startX = 50;
            int startY = height - 40;
            int chartHeight = height - 80;
            
            // Linha de base
            g2d.setColor(Color.WHITE);
            g2d.drawLine(startX, startY, startX + 6 * (barWidth + spacing), startY);
            
            for (int i = 0; i < 6; i++) {
                int value = estatisticas.getVitoriasPorTentativa(i+1);
                int barHeight = (int) ((double) value / maxValue * chartHeight);
                int x = startX + i * (barWidth + spacing);
                int y = startY - barHeight;
                
                // Barra
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(x, y, barWidth, barHeight);
                
                // Valor
                g2d.setColor(Color.WHITE);
                String valueStr = String.valueOf(value);
                int strWidth = g2d.getFontMetrics().stringWidth(valueStr);
                g2d.drawString(valueStr, x + (barWidth - strWidth) / 2, y - 5);
                
                // Tentativa
                String tentativa = (i + 1) + "ª";
                int tentWidth = g2d.getFontMetrics().stringWidth(tentativa);
                g2d.drawString(tentativa, x + (barWidth - tentWidth) / 2, startY + 20);
            }
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(400, 200);
        }
    }
    
    // método para exibir estatísticas
    public void mostrarEstatisticas() {
        Estatisticas stats = Estatisticas.carregarEstatisticas();
        String palavraAtual = jogo.getPalavraSecreta();
        String rank = determinarRankJogador(stats);
        Color rankColor = getRankColor(rank);

        if (stats.desafioHojeCompleto()) {
            String infoDesafio = "Desafio de hoje: COMPLETO | Próximo em: " + stats.tempoRestanteProximoDesafio();

            JLabel desafioLabel = new JLabel(infoDesafio);
            desafioLabel.setForeground(Color.CYAN);
            desafioLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        }

        JDialog dialog = new JDialog(frame, "Estatísticas", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(frame);
        dialog.getContentPane().setBackground(new Color(30, 30, 40));
        dialog.setUndecorated(true);
        dialog.setShape(new RoundRectangle2D.Double(0, 0, 600, 700, 40, 40));

        // Cabeçalho com efeito especial
        JPanel header = new GradientPanel(new Color(50, 50, 60), new Color(70, 70, 100));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        JLabel title = new JLabel("ESTATÍSTICAS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(255, 215, 0));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        header.add(title, BorderLayout.CENTER);
        
        // Botão de fechar
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dialog.dispose());
        closeBtn.setToolTipText("Fechar");
        header.add(closeBtn, BorderLayout.EAST);
        
        dialog.add(header, BorderLayout.NORTH);

        // Corpo principal
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        content.setBackground(new Color(40, 40, 50));
        content.setOpaque(true);

        // Painel de informações principais
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        infoPanel.setBackground(new Color(40, 40, 50));
        infoPanel.setOpaque(false);

        addStatCard(infoPanel, "🎮 PARTIDAS", String.valueOf(stats.getPartidasJogadas()), new Color(70, 130, 180));
        addStatCard(infoPanel, "🏆 VITÓRIAS", stats.getVitorias() + " (" + String.format("%.1f", stats.getPorcentagemVitorias()) + "%)", new Color(46, 204, 113));
        addStatCard(infoPanel, "🔥 SEQUÊNCIA", String.valueOf(stats.getStreak()), new Color(231, 76, 60));
        addStatCard(infoPanel, "🚀 MELHOR SEQ", String.valueOf(stats.getMaxStreak()), new Color(155, 89, 182));
        
        content.add(infoPanel);

        // Painel de rank
        JPanel rankPanel = new JPanel(new BorderLayout());
        rankPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        rankPanel.setOpaque(false);

        JLabel rankLabel = new JLabel("SEU RANK: " + rank);
        rankLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        rankLabel.setForeground(rankColor);
        rankLabel.setHorizontalAlignment(SwingConstants.CENTER);

        rankLabel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(2, 0, 2, 0, rankColor),
            new EmptyBorder(10, 0, 10, 0)
        ));

        rankPanel.add(rankLabel, BorderLayout.CENTER);
        content.add(rankPanel);

        // Painel da palavra atual
        if (palavraAtual != null && !palavraAtual.isEmpty()) {
            JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
            wordPanel.setOpaque(false);
            
            JLabel wordLabel = new JLabel("Última palavra: ");
            wordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            wordLabel.setForeground(new Color(180, 180, 220));
            wordPanel.add(wordLabel);
            
            for (char c : palavraAtual.toCharArray()) {
                JLabel charLabel = new JLabel(String.valueOf(c));
                charLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
                charLabel.setForeground(new Color(255, 215, 0));
                charLabel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100), 1));
                charLabel.setOpaque(true);
                charLabel.setBackground(new Color(50, 50, 70));
                charLabel.setPreferredSize(new Dimension(40, 40));
                charLabel.setHorizontalAlignment(SwingConstants.CENTER);
                wordPanel.add(charLabel);
            }
            
            content.add(wordPanel);
            content.add(Box.createVerticalStrut(15));
        }

        // Painel de distribuição de vitórias
        JPanel distPanel = new JPanel();
        distPanel.setLayout(new BoxLayout(distPanel, BoxLayout.Y_AXIS));

        Border lineBorder = BorderFactory.createLineBorder(new Color(80, 80, 100));
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            lineBorder,
            "DISTRIBUIÇÃO DE VITÓRIAS",
            TitledBorder.CENTER,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 16),
            new Color(200, 200, 220)
        );

        distPanel.setBorder(titledBorder);
        distPanel.setBackground(new Color(40, 40, 50));
        distPanel.setOpaque(false);

        int maxValue = stats.getMaxBarValue() == 0 ? 1 : stats.getMaxBarValue();
        Color[] barColors = {
            new Color(46, 204, 113),   // Verde
            new Color(52, 152, 219),   // Azul
            new Color(155, 89, 182),   // Roxo
            new Color(241, 196, 15),   // Amarelo
            new Color(230, 126, 34),   // Laranja
            new Color(231, 76, 60)     // Vermelho
        };
        
        for (int i = 0; i < 6; i++) {
            int tentativa = i + 1;
            int value = stats.getVitoriasPorTentativa(tentativa);
            
            JPanel barPanel = new JPanel(new BorderLayout(10, 0));
            barPanel.setBackground(new Color(40, 40, 50));
            barPanel.setOpaque(false);
            
            JLabel label = new JLabel(tentativa + "ª tentativa: ");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setForeground(new Color(200, 200, 220));
            label.setPreferredSize(new Dimension(120, 30));
            barPanel.add(label, BorderLayout.WEST);
            
            JProgressBar bar = new JProgressBar(0, maxValue);
            bar.setValue(value);
            bar.setString(value > 0 ? String.valueOf(value) : "");
            bar.setStringPainted(true);
            bar.setForeground(barColors[i]);
            bar.setBackground(new Color(60, 60, 70));
            bar.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
            bar.setStringPainted(true);
            
            // Customizar a string para mostrar porcentagem quando possível
            if (stats.getVitorias() > 0 && value > 0) {
                int percent = (int) Math.round((value * 100.0) / stats.getVitorias());
                bar.setString(value + " (" + percent + "%)");
            }
            
            barPanel.add(bar, BorderLayout.CENTER);
            distPanel.add(barPanel);
            distPanel.add(Box.createVerticalStrut(8));
        }
        
        content.add(distPanel);
        content.add(Box.createVerticalStrut(20));

        // Botões
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50));
        
        JButton resetButton = createStatsButton("RESETAR", new Color(231, 76, 60));
        resetButton.addActionListener(e -> {
            new Estatisticas().salvar();
            dialog.dispose();
            mostrarEstatisticas();
        });
        
        JButton closeButton = createStatsButton("FECHAR", new Color(52, 152, 219));
        closeButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        content.add(buttonPanel);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
        
        // Efeito de entrada
        // Efeito de entrada
new Timer(50, new ActionListener() {
    float opacity = 0f;
    public void actionPerformed(ActionEvent e) {
        if (opacity < 1f) {
            opacity += 0.1f;
            if (opacity > 1f) opacity = 1f; // Garantir que não ultrapasse 1.0
            dialog.setOpacity(opacity);
        } else {
            ((Timer)e.getSource()).stop();
        }
    }
}).start();
    }

    private void addStatCard(JPanel panel, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 50, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(color.darker(), 2),
            new EmptyBorder(15, 10, 15, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(color.brighter());
        card.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(valueLabel, BorderLayout.CENTER);
        
        panel.add(card);
    }

    private JButton createStatsButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
            new LineBorder(color.darker(), 2),
            new EmptyBorder(10, 20, 10, 20)
        ));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    // Classe para painel com gradiente
    class GradientPanel extends JPanel {
        private Color color1, color2;
        
        public GradientPanel(Color color1, Color color2) {
            this.color1 = color1;
            this.color2 = color2;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
    
    // Método para determinar a cor do rank
    private Color getRankColor(String rank) {
        if (rank.contains("LENDA SUPREMA")) return new Color(255, 215, 0); // Ouro
        if (rank.contains("MITO ATEMPORAL")) return new Color(192, 192, 192); // Prata
        if (rank.contains("MESTRE SUPREMO")) return new Color(205, 127, 50); // Bronze
        if (rank.contains("SÁBIO")) return new Color(0, 255, 255); // Ciano
        if (rank.contains("VETERANO")) return new Color(0, 255, 0); // Verde
        if (rank.contains("MAGO")) return new Color(138, 43, 226); // Violeta
        if (rank.contains("APRENDIZ")) return new Color(255, 165, 0); // Laranja
        if (rank.contains("INICIANTE")) return new Color(0, 191, 255); // Azul claro
        return Color.WHITE; // Branco para novato
    }
    
    // Método para determinar o rank do jogador
    private String determinarRankJogador(Estatisticas estatisticas) {
        int vitorias = estatisticas.getVitorias();
        int streak = estatisticas.getStreak();
        int maxStreak = estatisticas.getMaxStreak();
        double winRate = estatisticas.getPorcentagemVitorias();
        
        if (vitorias >= 50 && winRate > 80 && maxStreak >= 20) 
            return "LENDA SUPREMA DO TERMO";
        if (vitorias >= 30 && winRate > 70 && maxStreak >= 15) 
            return "MITO ATEMPORAL DO TERMO";
        if (vitorias >= 20 && winRate > 60 && maxStreak >= 10) 
            return "MESTRE SUPREMO DO TERMO";
        if (vitorias >= 15 && winRate > 50 && streak >= 5) 
            return "SÁBIO DAS PALAVRAS";
        if (vitorias >= 10 && winRate > 40) 
            return "VETERANO DO TERMO";
        if (vitorias >= 5 && winRate > 30) 
            return "MAGO DAS LETRAS";
        if (vitorias >= 3) 
            return "APRENDIZ DESTEMIDO";
        if (vitorias >= 1) 
            return "INICIANTE ENTUSIASTA";
        return "NOVATO DETERMINADO";
    }
    
    private void verificarConquistas() {
        List<String> conquistas = new ArrayList<>();
        if (jogo.getEstatisticasObj().getVitorias() >= 10) {
            conquistas.add("Veterano: 10 vitórias");
        }
        if (jogo.getEstatisticasObj().getStreak() >= 5) {
            conquistas.add("Foguete: 5 vitórias consecutivas");
        }
        if (jogo.getTentativasRestantes() == 6) {
            conquistas.add("Perfeição: Vitória na primeira tentativa");
        }
        if (!conquistas.isEmpty()) {
            StringBuilder msg = new StringBuilder("<html>Conquistas desbloqueadas!<ul>");
            for (String c : conquistas) {
                msg.append("<li>").append(c).append("</li>");
            }
            msg.append("</ul></html>");
            JOptionPane.showMessageDialog(frame, msg.toString(), "Conquistas!", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public int getTentativaAtual() {
        return tentativaAtual;
    }
    
    private void animarVitoria() {
        for (int i = 0; i < tamanhoPalavra; i++) {
            final int index = i;
            Timer timer = new Timer(300 * i, e -> {
                tiles[tentativaAtual][index].setBackground(
                    currentTheme.getBackgroundColor(Jogo.LetterState.CORRETA)
                );
                tiles[tentativaAtual][index].setForeground(
                    currentTheme.getTextColor(Jogo.LetterState.CORRETA)
                );
            });
            timer.setRepeats(false);
            timer.start();
        }
        
        Timer confettiTimer = new Timer(50, e -> {
            int x = random.nextInt(frame.getWidth());
            int y = random.nextInt(frame.getHeight() / 2);
            Color color = new Color(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
            );
            JLabel confetti = new JLabel("★");
            confetti.setFont(new Font("Arial", Font.BOLD, 24));
            confetti.setForeground(color);
            confetti.setBounds(x, y, 30, 30);
            frame.getLayeredPane().add(confetti, JLayeredPane.POPUP_LAYER);
            
            Timer fallTimer = new Timer(20, evt -> {
                confetti.setLocation(confetti.getX(), confetti.getY() + 5);
                if (confetti.getY() > frame.getHeight()) {
                    ((Timer) evt.getSource()).stop();
                    frame.getLayeredPane().remove(confetti);
                    frame.repaint();
                }
            });
            fallTimer.start();
        });
        confettiTimer.setRepeats(true);
        confettiTimer.start();
        new Timer(3000, e -> confettiTimer.stop()).start();
    }
}
