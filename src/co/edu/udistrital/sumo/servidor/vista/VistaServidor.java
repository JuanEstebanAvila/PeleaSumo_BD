package co.edu.udistrital.sumo.servidor.vista;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;


/**
 * Vista del servidor: muestra el dohyo, los luchadores, el log del combate
 * y el ganador.
 * Recursos en Data/Servidor/Recursos/ e imagenes de tecnicas en
 * Data/Servidor/Imagenes_tecnicas/.
 * No contiene logica de negocio ni objetos del modelo.
 *
 * @author Grupo Programacion Avanzada
 */
public class VistaServidor extends JFrame {

    private static final String REC  = "Data/Cliente/Recursos/";
    private static final String KTEC = "Data/Servidor/Imagenes_tecnicas/";

    private static final Color C_ROJO      = new Color(237, 85, 90);
    private static final Color C_AZUL      = new Color(55, 80, 170);
    private static final Color C_ROJO_DARK = new Color(130, 30, 30);
    private static final Color C_DORADO    = new Color(220, 185, 40);
    private static final Color C_BLANC     = Color.WHITE;

    private boolean combateEnCurso = false;

    private final JLabel      lblL1a, lblL1b, lblL2a, lblL2b;
    private final JLabel      lblGanador;
    private final JPanel      panelGanador;
    private final JTextArea   areaLog;
    private final JLabel      lblEstado;
    private final PanelCombate panelCombate;

    public VistaServidor() {
        super("Servidor - Combate de Sumo | Dohyo");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (!combateEnCurso) System.exit(0);
            }
        });

        lblL1a = lbl("LUCHADOR 1", Font.BOLD,   14);
        lblL1b = lbl("(Esperando...)", Font.ITALIC, 12);
        lblL2a = lbl("LUCHADOR 2", Font.BOLD,   14);
        lblL2b = lbl("(Esperando...)", Font.ITALIC, 12);

        lblGanador = new JLabel(" ", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Serif", Font.BOLD, 17));

        panelGanador = new JPanel(new BorderLayout());
        panelGanador.setBackground(C_DORADO);
        panelGanador.setBorder(new EmptyBorder(12, 16, 12, 16));
        panelGanador.add(lblGanador, BorderLayout.CENTER);

        areaLog      = crearAreaLog();
        lblEstado    = new JLabel("Esperando luchadores...", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Serif", Font.BOLD, 13));
        lblEstado.setForeground(C_BLANC);
        panelCombate = new PanelCombate();

        construirUI();
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Color.BLACK);
        setContentPane(raiz);
        raiz.add(construirHeader(), BorderLayout.NORTH);
        raiz.add(construirCentro(), BorderLayout.CENTER);
    }

    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_ROJO);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.X_AXIS));
        contenido.setOpaque(false);
        contenido.add(new JLabel(escalarIcono(REC + "Logo_Sumo.png", 58, 58)));
        contenido.add(Box.createRigidArea(new Dimension(12, 0)));
        JLabel t = new JLabel("¡\uD83D\uDC4A COMBATE DE SUMO \uD83D\uDC4A!");
        t.setFont(new Font("Serif", Font.BOLD, 36)); t.setForeground(C_BLANC);
        contenido.add(t);
        contenido.add(Box.createRigidArea(new Dimension(12, 0)));
        contenido.add(new JLabel(escalarIcono(REC + "japones.png", 58, 58)));
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false); centro.add(contenido);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirCentro() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel izq = new JPanel(new BorderLayout());
        izq.setOpaque(false);
        izq.add(panelCombate, BorderLayout.CENTER);
        izq.add(construirInfoLuchadores(), BorderLayout.SOUTH);
        p.add(izq, BorderLayout.CENTER);
        p.add(construirPanelLog(), BorderLayout.EAST);
        return p;
    }

    private JPanel construirInfoLuchadores() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel panelL1 = construirPanelNombre(lblL1a, lblL1b, C_AZUL);
        JPanel panelL2 = construirPanelNombre(lblL2a, lblL2b, C_ROJO_DARK);
        JLayeredPane capas = new JLayeredPane() {
            @Override public void doLayout() {
                int w = getWidth(), h = getHeight();
                panelL1.setBounds(0, 0, w / 2, h);
                panelL2.setBounds(w / 2, 0, w / 2, h);
                int vsW = 90, vsH = 65;
                Component vs = getComponentCount() > 2 ? getComponent(0) : null;
                if (vs != null) vs.setBounds(w / 2 - vsW / 2, h / 2 - vsH / 2, vsW, vsH);
            }
        };
        capas.setPreferredSize(new Dimension(0, 80));
        capas.add(panelL1, JLayeredPane.DEFAULT_LAYER);
        capas.add(panelL2, JLayeredPane.DEFAULT_LAYER);
        JLabel vs = new JLabel(escalarIcono(REC + "vs.png", 90, 65));
        vs.setHorizontalAlignment(SwingConstants.CENTER);
        capas.add(vs, JLayeredPane.PALETTE_LAYER);
        p.add(capas, BorderLayout.NORTH);
        p.add(panelGanador, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirPanelNombre(JLabel l1, JLabel l2, Color fondo) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(fondo); p.setBorder(new EmptyBorder(10, 16, 10, 16));
        p.add(l1); p.add(l2);
        return p;
    }

    private JPanel construirPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(360, 0));
        p.setBackground(C_ROJO);
        JLabel titulo = new JLabel("📋  Log del combate:", SwingConstants.LEFT);
        titulo.setFont(new Font("Serif", Font.BOLD, 18));
        titulo.setForeground(C_BLANC);
        titulo.setBorder(new EmptyBorder(16, 16, 8, 16));
        titulo.setBackground(C_ROJO); titulo.setOpaque(true);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel pEstado = new JPanel(new BorderLayout());
        pEstado.setBackground(C_AZUL);
        pEstado.setBorder(new EmptyBorder(10, 16, 10, 16));
        pEstado.add(lblEstado, BorderLayout.CENTER);
        p.add(titulo, BorderLayout.NORTH);
        p.add(scroll,  BorderLayout.CENTER);
        p.add(pEstado, BorderLayout.SOUTH);
        return p;
    }

    private JTextArea crearAreaLog() {
        JTextArea a = new JTextArea();
        a.setBackground(new Color(200, 70, 75)); a.setForeground(C_BLANC);
        a.setFont(new Font("Monospaced", Font.PLAIN, 12));
        a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 12, 8, 12));
        return a;
    }

    private JLabel lbl(String txt, int estilo, int tam) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("Serif", estilo, tam)); l.setForeground(C_BLANC);
        return l;
    }

    private Image cargarImagen(String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            if (ancho == -1) return raw.getImage();
            return raw.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        } catch (Exception e) { return null; }
    }

    private ImageIcon escalarIcono(String ruta, int w, int h) {
        Image img = cargarImagen(ruta, w, h);
        return img != null ? new ImageIcon(img) : new ImageIcon();
    }

    // --- API publica ---

    /** Agrega una linea al log con scroll automatico. */
    public void mostrarMensaje(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void actualizarEstado(String msg) { lblEstado.setText(msg); }

    /** Actualiza nombre y peso del luchador que llego. */
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        String info = nombre + " (" + String.format("%.1f", peso) + " kg)";
        if (indice == 0) { lblL1a.setText("LUCHADOR 1"); lblL1b.setText(info); }
        else             { lblL2a.setText("LUCHADOR 2"); lblL2b.setText(info); }
        panelCombate.repaint();
    }

    /** Inicia visualmente el combate y trae la ventana al frente. */
    public void mostrarInicioCombate(String n1, String n2) {
        combateEnCurso = true;
        actualizarEstado("COMBATE: " + n1 + " VS " + n2);
        setState(Frame.NORMAL);
        setAlwaysOnTop(true); toFront(); requestFocus(); setAlwaysOnTop(false);
    }

    /** Muestra la imagen del kimarite ejecutado. */
    public void mostrarKimarite(String luchador, String kimarite, boolean expulsado) {
        panelCombate.cargarKimarite(kimarite, expulsado);
        String res = expulsado ? "¡EXPULSADO!" : "El oponente resiste";
        actualizarEstado(luchador + " [" + kimarite + "] - " + res);
    }

    /** Muestra el ganador en el panel dorado. */
    public void mostrarGanador(String nombre, int victorias) {
        lblGanador.setText("🏆 GANADOR: " + nombre + "  |  Victorias: " + victorias);
        actualizarEstado("Combate finalizado. Ganador: " + nombre);
    }

    /** Cierra la ventana del servidor. Llamado por ControlVistaS. */
    public void cerrar() {
        dispose();
        System.exit(0);
    }

    // --- Inner class PanelCombate ---

    private class PanelCombate extends JPanel {
        private final Image imgFondo, imgL1, imgL2;
        private Image  imgKimarite = null;
        private String nomKimarite = "";
        private Color  colKimarite = Color.WHITE;

        public PanelCombate() {
            setOpaque(true);
            imgFondo = cargarImagen(REC + "Dohyo.png",     -1, -1);
            imgL1    = cargarImagen(REC + "Luchador1.png", -1, -1);
            imgL2    = cargarImagen(REC + "Luchador2.png", -1, -1);
        }

        public void cargarKimarite(String nombre, boolean expulsado) {
            nomKimarite = nombre;
            colKimarite = expulsado ? new Color(255, 80, 80) : new Color(100, 240, 130);
            imgKimarite = null;
            for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
                File f = new File(KTEC + nombre + ext);
                if (f.exists()) { imgKimarite = cargarImagen(KTEC + nombre + ext, -1, -1); break; }
            }
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,  RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();
            if (imgFondo != null) g2.drawImage(imgFondo, 0, 0, w, h, null);
            else { g2.setColor(new Color(60, 45, 30)); g2.fillRect(0, 0, w, h); }
            g2.setColor(new Color(0, 0, 0, 50)); g2.fillRect(0, 0, w, h);
            pintarLuchador(g2, imgL1, w, h, false);
            pintarLuchador(g2, imgL2, w, h, true);
            if (imgKimarite != null) pintarKimarite(g2, w, h);
            g2.dispose();
        }

        private void pintarLuchador(Graphics2D g2, Image img, int w, int h, boolean der) {
            if (img == null) return;
            int m = w / 2, maxW = (int)(m * 0.55), maxH = (int)(h * 0.68);
            double esc = Math.min((double) maxW / img.getWidth(null), (double) maxH / img.getHeight(null));
            int rw = (int)(img.getWidth(null) * esc), rh = (int)(img.getHeight(null) * esc);
            int cx = der ? (m + m / 2) : (m / 2);
            g2.drawImage(img, cx - rw / 2, (int)(h * 0.15), rw, rh, null);
        }

        private void pintarKimarite(Graphics2D g2, int w, int h) {
            int maxW = (int)(w * 0.28), maxH = (int)(h * 0.38);
            int origW = imgKimarite.getWidth(null), origH = imgKimarite.getHeight(null);
            if (origW <= 0 || origH <= 0) return;
            double esc = Math.min((double) maxW / origW, (double) maxH / origH);
            int rw = (int)(origW * esc), rh = (int)(origH * esc);
            int kx = w / 2 - rw / 2, ky = (int)(h * 0.32) - rh / 2;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(kx - 12, ky - 10, rw + 24, rh + 30, 14, 14);
            g2.drawImage(imgKimarite, kx, ky, rw, rh, null);
            g2.setFont(new Font("Serif", Font.BOLD, 13)); g2.setColor(colKimarite);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nomKimarite, w / 2 - fm.stringWidth(nomKimarite) / 2, ky + rh + 18);
        }
    }
}
