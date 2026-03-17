import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class TelaInicial extends JFrame {
    private JTextField nomeField;
    private JComboBox<Integer> tamanhoCombo;
    private JComboBox<String> temaCombo;
    private Timer animationTimer;
    private float hueShift = 0;
    private final String[] REGRAS = {
        "REGRAS DO TERMO:",
        "1. Adivinhe a palavra secreta em até 6 tentativas",
        "2. Cada tentativa deve ser uma palavra válida",
        "3. Após cada tentativa, as cores indicam:",
        "   - VERDE: letra na posição correta",
        "   - AMARELO: letra na palavra, mas posição errada",
        "   - CINZA: letra não está na palavra"
    };

    public TelaInicial() {
        setTitle("TERMO - Início");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 800, 600, 40, 40));

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color color1 = Color.getHSBColor(hueShift, 0.7f, 0.2f);
                Color color2 = Color.getHSBColor(hueShift + 0.4f, 0.7f, 0.2f);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
                String title = "TERMO COLORIDO";

                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.drawString(title, 204, 104);

                GradientPaint titleGradient = new GradientPaint(
                        200, 100, Color.getHSBColor(hueShift + 0.2f, 0.9f, 0.9f),
                        600, 100, Color.getHSBColor(hueShift + 0.6f, 0.9f, 0.9f)
                );
                g2d.setPaint(titleGradient);
                g2d.drawString(title, 200, 100);
            }
        };

        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        controlsPanel.setOpaque(false);

        JButton minimizeBtn = createControlButtonWithIcon("−", new Color(255, 204, 0));
        JButton closeBtn = createControlButtonWithIcon("×", new Color(255, 80, 80));

        minimizeBtn.addActionListener(e -> setState(Frame.ICONIFIED));
        closeBtn.addActionListener(e -> System.exit(0));

        controlsPanel.add(minimizeBtn);
        controlsPanel.add(closeBtn);
        headerPanel.add(controlsPanel, BorderLayout.EAST);

        JPanel dragPanel = new JPanel();
        dragPanel.setOpaque(false);
        final Point[] mouseDownPoint = new Point[1];
        dragPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseDownPoint[0] = e.getPoint();
            }
        });
        dragPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (mouseDownPoint[0] != null) {
                    Point currentPoint = e.getLocationOnScreen();
                    setLocation(
                            currentPoint.x - mouseDownPoint[0].x,
                            currentPoint.y - mouseDownPoint[0].y
                    );
                }
            }
        });
        dragPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        headerPanel.add(dragPanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
        rulesPanel.setOpaque(false);
        rulesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesPanel.setBorder(new EmptyBorder(10, 50, 20, 50));

        for (String regra : REGRAS) {
            JLabel ruleLabel = new JLabel(regra);
            ruleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            ruleLabel.setForeground(new Color(220, 220, 255));
            ruleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            ruleLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
            rulesPanel.add(ruleLabel);
        }
        contentPanel.add(rulesPanel);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setOpaque(false);
        inputPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        inputPanel.setBorder(new EmptyBorder(20, 100, 20, 100));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nomeLabel = new JLabel("Seu nome:");
        nomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nomeLabel.setForeground(new Color(240, 240, 255));
        inputPanel.add(nomeLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        nomeField = new JTextField(15);
        nomeField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        inputPanel.add(nomeField, gbc);

        // foco automático no campo nome
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel tamanhoLabel = new JLabel("Tamanho da palavra:");
        tamanhoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tamanhoLabel.setForeground(new Color(240, 240, 255));
        inputPanel.add(tamanhoLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        tamanhoCombo = new JComboBox<>(new Integer[]{3, 4, 5, 6, 7});
        tamanhoCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        tamanhoCombo.setSelectedItem(5);
        tamanhoCombo.setBackground(new Color(60, 60, 90));
        tamanhoCombo.setForeground(new Color(240, 240, 255));
        inputPanel.add(tamanhoCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel temaLabel = new JLabel("Tema do jogo:");
        temaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        temaLabel.setForeground(new Color(240, 240, 255));
        inputPanel.add(temaLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        temaCombo = new JComboBox<>(new String[]{"Escuro", "Claro", "Colorido", "Retrô"});
        temaCombo.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        temaCombo.setBackground(new Color(60, 60, 90));
        temaCombo.setForeground(new Color(240, 240, 255));
        inputPanel.add(temaCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton infoButton = new JButton("Ver Cores do Tema");
        infoButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoButton.setPreferredSize(new Dimension(180, 25));
        infoButton.setBackground(new Color(80, 80, 120));
        infoButton.setForeground(Color.WHITE);
        infoButton.setOpaque(true);
        infoButton.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 150)));
        infoButton.addActionListener(e -> mostrarLegendaCores());
        inputPanel.add(infoButton, gbc);

        // adiciona inputPanel ao contentPanel
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        // --- CRIA O PAINEL DE BOTÕES (garante visibilidade no rodapé) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton startButton = createGlassButton("COMEÇAR JOGO");
        JButton dailyButton = createGlassButton("DESAFIO DIÁRIO");
        JButton exitButton = createGlassButton("SAIR");

        // Adiciona ação ao botão de desafio diário
        dailyButton.addActionListener(e -> {
            Estatisticas stats = Estatisticas.carregarEstatisticas();
            stats.resetDesafioDiario();
            
            if (stats.podeJogarDesafio()) {
                String palavraDoDia = DesafioDiario.getPalavraDoDia(stats);
                if (palavraDoDia != null) {
                    // Inicia o jogo do desafio diário
                    iniciarJogo(true, palavraDoDia);
                } else {
                    JOptionPane.showMessageDialog(TelaInicial.this, 
                        "Erro ao obter a palavra do dia.", 
                        "Erro", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(TelaInicial.this, 
                    "Você já completou o desafio de hoje!\nPróximo desafio em: " + stats.tempoRestanteProximoDesafio(), 
                    "Desafio Diário", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        startButton.addActionListener(e -> iniciarJogo(false, null));
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(startButton);
        buttonPanel.add(dailyButton);
        buttonPanel.add(exitButton);

        // --- CRIA UM BOTTOM PANEL QUE CONTÉM OS BOTÕES E AS ESTATÍSTICAS ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // Painel de estatísticas
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statsPanel.setOpaque(false);
        
        // Carregar estatísticas
        Estatisticas stats = Estatisticas.carregarEstatisticas();
        JLabel statsLabel = new JLabel(String.format(
            "Estatísticas: %d vitórias - %d derrotas - Streak: %d",
            stats.getVitorias(),
            stats.getPartidasJogadas() - stats.getVitorias(),
            stats.getStreak()
        ));
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(Color.WHITE);
        statsPanel.add(statsLabel);

        // adicionar botão centralizado e stats abaixo
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statsPanel, BorderLayout.SOUTH);

        // adiciona o content no centro e o bottom na parte sul da mainPanel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // animação do fundo
        animationTimer = new Timer(30, e -> {
            hueShift = (hueShift + 0.005f) % 1.0f;
            mainPanel.repaint();
        });
        animationTimer.start();

        setVisible(true);
    }

    private JButton createControlButtonWithIcon(String iconText, Color bgColor) {
        JButton btn = new JButton(iconText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 30));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setPreferredSize(new Dimension(50, 50));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void mostrarLegendaCores() {
        String temaEscolhido = (String) temaCombo.getSelectedItem();
        String mensagem = "<html><div style='text-align: center;'>"
            + "<h2>Legenda de Cores - " + temaEscolhido + "</h2>"
            + "<p>As cores indicam o seguinte:</p>"
            + "<table align='center' cellpadding='5'>"
            + "<tr><td bgcolor='#2ECC71' width='30'>&nbsp;</td><td>Letra na posição correta</td></tr>"
            + "<tr><td bgcolor='#F1C40F' width='30'>&nbsp;</td><td>Letra existe mas em outra posição</td></tr>"
            + "<tr><td bgcolor='#95A5A6' width='30'>&nbsp;</td><td>Letra não existe na palavra</td></tr>"
            + "</table></div></html>";

        JOptionPane.showMessageDialog(this, mensagem, "Legenda de Cores", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createGlassButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setPreferredSize(new Dimension(180, 45));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 160, 210));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }

    private void iniciarJogo(boolean isDailyChallenge, String palavraDoDia) {
        String nomeInput = nomeField.getText().trim();
        final String nomeFinal = nomeInput.isEmpty() ? "Jogador" : nomeInput;
        
        // Para desafio diário, forçar tamanho 5 e usar a palavra do dia
        final int tamanhoFinal;
        final String palavraFixa;
        
        if (isDailyChallenge) {
            tamanhoFinal = 5;
            palavraFixa = palavraDoDia;
            System.out.println("Desafio diário - Palavra: " + palavraFixa);
        } else {
            tamanhoFinal = (int) tamanhoCombo.getSelectedItem();
            palavraFixa = null;
        }
        
        final String temaEscolhido = (String) temaCombo.getSelectedItem();

        // Fechar a tela inicial
        dispose();
        animationTimer.stop();

        // Executar em nova thread para evitar bloqueio
        new Thread(() -> {
            try {
                // Pequeno delay para garantir que a tela anterior foi fechada
                Thread.sleep(100);
                SwingUtilities.invokeLater(() -> {
                    new InterfaceGrafica(nomeFinal, tamanhoFinal, palavraFixa, temaEscolhido, isDailyChallenge);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        // Inicializar o sistema de som
        //SoundManager.initialize();
        
        SwingUtilities.invokeLater(() -> {
            new TelaInicial();
        });
    }
}
