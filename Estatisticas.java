import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class Estatisticas implements Serializable {
    private int partidasJogadas;
    private int vitorias;
    private int streak;
    private int maxStreak;
    private int[] vitoriasPorTentativa;
    private LocalDate ultimoDesafio;
    private boolean desafioCompletoHoje;
    private static final long serialVersionUID = 6L; 

    public Estatisticas() {
        reset();
    }

    public void reset() {
        partidasJogadas = 0;
        vitorias = 0;
        streak = 0;
        maxStreak = 0;
        vitoriasPorTentativa = new int[6];
        ultimoDesafio = null;
        desafioCompletoHoje = false;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (vitoriasPorTentativa == null) {
            vitoriasPorTentativa = new int[6];
        }
        if (ultimoDesafio == null) {
            ultimoDesafio = null;
        }
        try {
            desafioCompletoHoje = ois.readBoolean();
        } catch (Exception e) {
            desafioCompletoHoje = false;
        }
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeBoolean(desafioCompletoHoje);
    }

    public boolean podeJogarDesafio() {
        // Se nunca jogou ou se a última vez foi em um dia diferente
        if (ultimoDesafio == null || !ultimoDesafio.equals(LocalDate.now())) {
            return true;
        }
        // Se jogou hoje mas não completou
        return !desafioCompletoHoje;
    }

    public void registrarDesafioCompleto() {
        this.ultimoDesafio = LocalDate.now();
        this.desafioCompletoHoje = true;
        salvar();
    }

    public void resetDesafioDiario() {
        // Se é um novo dia, reseta o status do desafio
        if (ultimoDesafio != null && !ultimoDesafio.equals(LocalDate.now())) {
            desafioCompletoHoje = false;
        }
    }

    public boolean desafioHojeCompleto() {
        return desafioCompletoHoje && ultimoDesafio != null && ultimoDesafio.equals(LocalDate.now());
    }

    public String tempoRestanteProximoDesafio() {
        if (!desafioHojeCompleto()) return "0h 0m 0s";
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime meiaNoite = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        
        long segundosRestantes = ChronoUnit.SECONDS.between(agora, meiaNoite);
        long horas = segundosRestantes / 3600;
        long minutos = (segundosRestantes % 3600) / 60;
        long segundos = segundosRestantes % 60;
        
        return String.format("%dh %dm %ds", horas, minutos, segundos);
    }
    
    public String getDataUltimoDesafio() {
        if (ultimoDesafio == null) return "Nunca";
        return ultimoDesafio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public int getPartidasJogadas() {
        return partidasJogadas;
    }

    public int getVitorias() {
        return vitorias;
    }

    public int getStreak() {
        return streak;
    }

    public int getMaxStreak() {
        return maxStreak;
    }

    public double getPorcentagemVitorias() {
        if (partidasJogadas == 0) return 0.0;
        return Math.round((vitorias * 100.0) / partidasJogadas * 10) / 10.0;
    }
    
    public int getVitoriasPorTentativa(int tentativa) {
        if (tentativa < 1 || tentativa > 6) return 0;
        return vitoriasPorTentativa[tentativa - 1];
    }

    public int getMaxBarValue() {
        int max = 0;
        for (int i = 0; i < 6; i++) {
            if (vitoriasPorTentativa[i] > max) {
                max = vitoriasPorTentativa[i];
            }
        }
        return max == 0 ? 1 : max;
    }

    public void salvar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("estatisticas.dat"))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Erro ao salvar estatísticas: " + e.getMessage());
        }
    }
    
    public void registrarVitoria(int tentativasUsadas) {
        partidasJogadas++;
        vitorias++;
        streak++;
        if (streak > maxStreak) maxStreak = streak;
        
        if (tentativasUsadas >= 1 && tentativasUsadas <= 6) {
            vitoriasPorTentativa[tentativasUsadas - 1]++;
        }
        salvar();
    }     

    public void registrarDerrota() {
        partidasJogadas++;
        streak = 0;
        salvar();
    }

    public static Estatisticas carregarEstatisticas() {
        File file = new File("estatisticas.dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Estatisticas) ois.readObject();
            } catch (InvalidClassException e) {
                file.delete();
                return new Estatisticas();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Erro ao carregar estatísticas: " + e.getMessage());
                return new Estatisticas();
            }
        }
        return new Estatisticas();
    }
}
