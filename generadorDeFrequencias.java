import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionListener;

public class generadorDeFrequencias {
    private static String[] notas = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    private static String[] calidades = { "", "7", "Maj7", "m", "m7", "mMaj7" };

    private static double c4 = 261.63;
    private static double ratio = 1.059464062;
    private static int d = 2;
    private static int e = 4;
    private static int f = 5;
    private static int g = 7;
    private static int a = 9;
    private static int b = 11;
    private static int tempo = 500;
    protected static final int SAMPLE_RATE = 5000;// 16 * 1024;
    private static SourceDataLine line;
    private static Map<String, double[]> frecuencias = new HashMap<>();

    public static void main(String[] args) {

        JPanel p = new JPanel(new GridLayout(calidades.length, notas.length));

        for (int i = 0; i < calidades.length; i++) {
            for (int j = 0; j < notas.length; j++) {

                String clave = notas[j] + calidades[i];
                double[] valor = freqs(clave);
                frecuencias.put(clave, valor);

                JButton boton = new JButton(notas[j] + calidades[i]);
                boton.addActionListener((ActionListener) oyenteAccion);
                p.add(boton);

            }
        }

        JFrame frame = new JFrame("Mi Aplicación");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLayout(new BorderLayout());
        frame.add(p, BorderLayout.CENTER);
        frame.setVisible(true);

    }

    private static ActionListener oyenteAccion = actionEvent -> {
        try {
            reproductor(actionEvent.getActionCommand());
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    };

    public static double[] freqs(String chord) {
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

        if (chord.indexOf('D') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += d;
        }

        if (chord.indexOf('E') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += e;
        }

        if (chord.indexOf('F') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += f;
        }

        if (chord.indexOf('G') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += g;
        }

        if (chord.indexOf('A') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += a;
        }

        if (chord.indexOf('B') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += b;
        }

        if (chord.indexOf('#') != -1) {
            for (int i = 0; i < notas.length; i++)
                notas[i] += 1;
        }
        //////////////////////////////////////////////////////////////////////////
/*         for (int i = 0; i < notas.length; i++) {
            notas[i] = notas[i] % 12;
        } */
        /////////////////////////////////////////////////////////////////////////////
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

    public static void reproductor(String chord) throws LineUnavailableException {

        double[] freqs = frecuencias.get(chord);
        byte[] toneBuffer = arregloOnda(freqs, tempo);

        if (line == null || !line.isOpen()) {
            line = AudioSystem.getSourceDataLine(new AudioFormat(SAMPLE_RATE, 8, 1, true, true));
            line.open(new AudioFormat(SAMPLE_RATE, 8, 1, true, true), SAMPLE_RATE);
            line.start();
        }

        line.write(toneBuffer, 0, toneBuffer.length);
        line.drain();
        line.close();
    }
}