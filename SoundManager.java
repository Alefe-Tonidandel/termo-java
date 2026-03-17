import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SoundManager {
    private static Clip correctSound;
    private static Clip wrongSound;
    private static Clip winSound;
    private static Clip loseSound;
    private static Clip keyPressSound;
    private static Clip backgroundMusic;
    
    private static final AtomicBoolean muted = new AtomicBoolean(false);
    private static final AtomicBoolean backgroundMusicPlaying = new AtomicBoolean(false);
    private static final Map<Clip, String> CLIP_NAME = new HashMap<>();
    
    static {
        loadAllSounds();
    }
    
    private static void loadAllSounds() {
        try {
            correctSound = loadSound("correct.wav");
            wrongSound = loadSound("wrong.wav");
            winSound = loadSound("victory.wav");
            loseSound = loadSound("lose.wav");
            keyPressSound = loadSound("keypress.wav");
            backgroundMusic = loadSound("background-music.wav");
            
            System.out.println("Todos os sons carregados com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao carregar sons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Clip loadSound(String filename) {
        try {
            // Tenta carregar do classpath primeiro
            InputStream is = SoundManager.class.getClassLoader().getResourceAsStream("sounds/" + filename);
            if (is != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                CLIP_NAME.put(clip, filename);
                return clip;
            }
            
            // Se não encontrou no classpath, tenta carregar do sistema de arquivos
            File soundFile = new File("sounds/" + filename);
            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                CLIP_NAME.put(clip, filename);
                return clip;
            }
            
            System.err.println("Arquivo de som não encontrado: " + filename);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Erro ao carregar o som " + filename + ": " + e.getMessage());
        }
        return null;
    }
    
    private static void playSound(Clip clip) {
        if (muted.get() || clip == null) return;
        
        try {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
        } catch (Exception e) {
            System.err.println("Erro ao reproduzir som: " + e.getMessage());
        }
    }
    
    public static void startBackgroundMusic() {
        if (muted.get() || backgroundMusic == null) return;
        
        try {
            if (backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            backgroundMusic.setFramePosition(0);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
            backgroundMusicPlaying.set(true);
        } catch (Exception e) {
            System.err.println("Erro ao iniciar música de fundo: " + e.getMessage());
        }
    }
    
    public static void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
        backgroundMusicPlaying.set(false);
    }
    
    public static void stopAll() {
        stopBackgroundMusic();
        if (correctSound != null) correctSound.stop();
        if (wrongSound != null) wrongSound.stop();
        if (winSound != null) winSound.stop();
        if (loseSound != null) loseSound.stop();
        if (keyPressSound != null) keyPressSound.stop();
    }
    
    public static void toggleMute() {
        boolean now = !muted.get();
        muted.set(now);
        
        if (now) {
            stopBackgroundMusic();
        } else if (backgroundMusicPlaying.get()) {
            startBackgroundMusic();
        }
    }
    
    public static boolean isMuted() {
        return muted.get();
    }
    
    public static void playCorrect() {
        playSound(correctSound);
    }
    
    public static void playWrong() {
        playSound(wrongSound);
    }
    
    public static void playKeyPress() {
        playSound(keyPressSound);
    }
    
    public static void playWin() {
        stopBackgroundMusic();
        playSound(winSound);
    }
    
    public static void playLose() {
        stopBackgroundMusic();
        playSound(loseSound);
    }
}
