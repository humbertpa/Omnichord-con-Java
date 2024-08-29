import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GeneradorDeFrecuencias {
    private static String[] notas = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    private static String[] calidades = { "", "7", "Maj7", "m", "m7", "mMaj7" };

    private static double c4 = 261.63;
    private static double ratio = 1.059464062;
    private static int duracion = 500;
    protected static final int SAMPLE_RATE = 44100; // Usar una frecuencia de muestreo estándar
    private static SourceDataLine line;
    private static Map<String, double[]> frecuencias = new HashMap<>();
    private static Map<String, Integer> transpose = Map.of("C", 0, "D", 2, "E", 4, "F", 5, "G", 7, "A", 9, "B", 11);
    private static boolean isPlaying = false; // Estado de reproducción

    private static void inicializadorFrecuencias() {

        for (String calidad : calidades) {
            for (String nota : notas) {
                String clave = nota + calidad;
                double[] valor = freqs(clave);
                frecuencias.put(clave, valor);
            }
        }
    }

    private static void crearUI() {
        JPanel p = new JPanel(new GridLayout(calidades.length, notas.length));

        for (String calidad : calidades) {
            for (String nota : notas) {
                String clave = nota + calidad;
                JButton boton = new JButton(nota + calidad);
                boton.addMouseListener(new BotonMouseListener(clave));
                p.add(boton);
            }
        }

        // Crear y mostrar el marco
        JFrame frame = new JFrame("Mi Aplicación");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLayout(new BorderLayout());
        frame.add(p, BorderLayout.CENTER);
        frame.setVisible(true);

    }

    public static void main(String[] args) {

        inicializadorFrecuencias();
        crearUI();

    }

    private static class BotonMouseListener extends MouseAdapter {
        private final String chord;

        public BotonMouseListener(String chord) {
            this.chord = chord;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (!isPlaying) {
                isPlaying = true;
                new Thread(() -> {
                    try {
                        reproducirContinuamente(chord);
                    } catch (LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isPlaying = false;
            detenerReproduccion();
        }
    }

    private static void reproducirContinuamente(String chord) throws LineUnavailableException {
        double[] freqs = frecuencias.get(chord);
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        if (line == null || !line.isOpen()) {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, SAMPLE_RATE);
            line.start();
        }

        while (isPlaying) {
            byte[] toneBuffer = arregloOnda(freqs, duracion);
            line.write(toneBuffer, 0, toneBuffer.length);
        }
        line.drain();
    }

    private static void detenerReproduccion() {
        if (line != null && line.isOpen()) {
            line.stop();
            line.close();
        }
    }

    public static double[] freqs(String chord) {
        System.out.println(chord);
        int[] notas;
        if (chord.indexOf('7') != -1) {
            if (chord.contains("Maj")) {
                notas = new int[] { 0, 0, 7, 11 };
            } else {
                notas = new int[] { 0, 0, 7, 10 };
            }
        } else {
            notas = new int[] { 0, 0, 7 };
        }

        double[] acordes = new double[notas.length];

        if (chord.indexOf('m') != -1) {
            notas[1] = 3;
        } else {
            notas[1] = 4;
        }

        int transponer = transpose.get(String.valueOf(chord.charAt(0)));
        if (chord.length() > 1 && chord.charAt(1) == '#')
            transponer += 1;

        for (int i = 0; i < notas.length; i++)
            notas[i] += transponer;

        for (int i = 0; i < acordes.length; i++) {
            acordes[i] = c4 * Math.pow(ratio, notas[i]);
        }
        return acordes;
    }

    public static byte[] arregloOnda(double[] fs, int ms) {
        int samples = (ms * SAMPLE_RATE) / 1000;
        byte[] output = new byte[samples];
        double[] angles = new double[fs.length];
        for (int i = 0; i < output.length; i++) {
            double prom = 0;
            for (int j = 0; j < fs.length; j++) {
                angles[j] = 2.0 * Math.PI * i * fs[j] / SAMPLE_RATE;
            }
            for (int k = 0; k < angles.length; k++) {
                prom += Math.sin(angles[k]);
            }
            prom = prom / (float) (angles.length);
            output[i] = (byte) (prom * 127f);
        }
        return output;
    }
}
