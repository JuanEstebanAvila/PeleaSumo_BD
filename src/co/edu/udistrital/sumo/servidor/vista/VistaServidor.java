package co.edu.udistrital.sumo.servidor.vista;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista del servidor: interfaz grafica del torneo de sumo (MVC - Vista).
 * 
 * Esta clase SOLO se encarga de la presentacion grafica.
 * No contiene logica de negocio, ni objetos del modelo, ni SQL.
 * ControlVistaS delega a esta vista usando SwingUtilities.invokeLater.
 * 
 * Componentes principales:
 *   - gridConectados (2x3): grilla con 6 slots que muestran los luchadores
 *     conectandose en tiempo real. Cada slot tiene estados visuales con colores:
 *     Vacio (gris) -> Conectado (verde) -> Combatiendo (naranja)
 *     -> Ganador (dorado) -> Ya combatio (gris atenuado)
 *   - panelCombate: imagen del dohyo con los dos luchadores y las tecnicas
 *   - areaLog: JTextArea con scroll que muestra el log del combate
 *   - lblEstado: barra de estado inferior con el estado actual
 *   - panelGanador: panel dorado que muestra el nombre del ganador
 * 
 * Las imagenes de tecnicas se buscan en Data/Servidor/Imagenes_tecnicas/
 * con extensiones .png, .jpg, .jpeg.
 * 
 * Muestra el dohyo, los luchadores conectados,
 * la seleccion aleatoria, el log del combate y el ganador.
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
    private static final Color C_VERDE     = new Color(46, 139, 87);
    private static final Color C_GRIS      = new Color(80, 80, 80);
    private static final Color C_NARANJA   = new Color(230, 140, 30);

    private static final int MAX_LUCHADORES = 6;

    private boolean combateEnCurso = false;

    private final List<PanelLuchadorConectado> panelesConectados;
    private final JPanel                       gridConectados;
    private final JLabel                       lblContador;

    private final JLabel      lblL1a, lblL1b, lblL2a, lblL2b;
    private final JLabel      lblGanador;
    private final JPanel      panelGanador;
    private final JTextArea   areaLog;
    private final JLabel      lblEstado;
    private final PanelCombate panelCombate;

    public VistaServidor() {
        super("Servidor - Combate de Sumo | Dohyo");
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (!combateEnCurso) System.exit(0);
            }
        });

        panelesConectados = new ArrayList<>();
        gridConectados    = new JPanel(new GridLayout(2, 3, 8, 8));
        gridConectados.setOpaque(false);
        lblContador = new JLabel("Conectados: 0 / " + MAX_LUCHADORES,
                                  SwingConstants.CENTER);
        lblContador.setFont(new Font("Serif", Font.BOLD, 14));
        lblContador.setForeground(C_BLANC);

        for (int i = 0; i < MAX_LUCHADORES; i++) {
            PanelLuchadorConectado p = new PanelLuchadorConectado(i + 1);
            panelesConectados.add(p);
            gridConectados.add(p);
        }

        lblL1a = lbl("LUCHADOR 1", Font.BOLD, 14);
        lblL1b = lbl("(Esperando...)", Font.ITALIC, 12);
        lblL2a = lbl("LUCHADOR 2", Font.BOLD, 14);
        lblL2b = lbl("(Esperando...)", Font.ITALIC, 12);

        lblGanador = new JLabel(" ", SwingConstants.CENTER);
        lblGanador.setFont(new Font("Serif", Font.BOLD, 17));

        panelGanador = new JPanel(new BorderLayout());
        panelGanador.setBackground(C_DORADO);
        panelGanador.setBorder(new EmptyBorder(12, 16, 12, 16));
        panelGanador.add(lblGanador, BorderLayout.CENTER);

        areaLog   = crearAreaLog();
        lblEstado = new JLabel("Esperando luchadores...", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Serif", Font.BOLD, 13));
        lblEstado.setForeground(C_BLANC);
        panelCombate = new PanelCombate();

        construirUI();
    }

    // ======================= Construccion de la UI =======================

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
        t.setFont(new Font("Serif", Font.BOLD, 36));
        t.setForeground(C_BLANC);
        contenido.add(t);
        contenido.add(Box.createRigidArea(new Dimension(12, 0)));
        contenido.add(new JLabel(escalarIcono(REC + "japones.png", 58, 58)));
        JPanel centro = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centro.setOpaque(false);
        centro.add(contenido);
        p.add(centro, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirCentro() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel izq = new JPanel(new BorderLayout());
        izq.setOpaque(false);
        izq.add(construirPanelConectados(), BorderLayout.NORTH);
        izq.add(panelCombate, BorderLayout.CENTER);
        izq.add(construirInfoLuchadores(), BorderLayout.SOUTH);
        p.add(izq, BorderLayout.CENTER);
        p.add(construirPanelLog(), BorderLayout.EAST);
        return p;
    }

    private JPanel construirPanelConectados() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(30, 30, 50));
        wrapper.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel titulo = new JLabel("\uD83C\uDFAF  Luchadores en el Torneo",
                                    SwingConstants.LEFT);
        titulo.setFont(new Font("Serif", Font.BOLD, 16));
        titulo.setForeground(C_DORADO);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(titulo, BorderLayout.WEST);
        top.add(lblContador, BorderLayout.EAST);
        top.setBorder(new EmptyBorder(0, 0, 8, 0));

        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(gridConectados, BorderLayout.CENTER);
        wrapper.setPreferredSize(new Dimension(0, 150));
        return wrapper;
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
                Component vs = getComponentCount() > 2
                        ? getComponent(0) : null;
                if (vs != null)
                    vs.setBounds(w / 2 - vsW / 2, h / 2 - vsH / 2, vsW, vsH);
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
        p.setBackground(fondo);
        p.setBorder(new EmptyBorder(10, 16, 10, 16));
        p.add(l1);
        p.add(l2);
        return p;
    }

    private JPanel construirPanelLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(360, 0));
        p.setBackground(C_ROJO);
        JLabel titulo = new JLabel("\uD83D\uDCCB  Log del combate:",
                                    SwingConstants.LEFT);
        titulo.setFont(new Font("Serif", Font.BOLD, 18));
        titulo.setForeground(C_BLANC);
        titulo.setBorder(new EmptyBorder(16, 16, 8, 16));
        titulo.setBackground(C_ROJO);
        titulo.setOpaque(true);
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel pEstado = new JPanel(new BorderLayout());
        pEstado.setBackground(C_AZUL);
        pEstado.setBorder(new EmptyBorder(10, 16, 10, 16));
        pEstado.add(lblEstado, BorderLayout.CENTER);
        p.add(titulo, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(pEstado, BorderLayout.SOUTH);
        return p;
    }

    private JTextArea crearAreaLog() {
        JTextArea a = new JTextArea();
        a.setBackground(new Color(200, 70, 75));
        a.setForeground(C_BLANC);
        a.setFont(new Font("Monospaced", Font.PLAIN, 12));
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 12, 8, 12));
        return a;
    }

    private JLabel lbl(String txt, int estilo, int tam) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("Serif", estilo, tam));
        l.setForeground(C_BLANC);
        return l;
    }

    private Image cargarImagen(String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            if (ancho == -1) return raw.getImage();
            return raw.getImage().getScaledInstance(ancho, alto,
                    Image.SCALE_SMOOTH);
        } catch (Exception e) { return null; }
    }

    private ImageIcon escalarIcono(String ruta, int w, int h) {
        Image img = cargarImagen(ruta, w, h);
        return img != null ? new ImageIcon(img) : new ImageIcon();
    }

    // ======================= API publica =======================

    public void mostrarMensaje(String msg) {
        areaLog.append(msg + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public void actualizarEstado(String msg) {
        lblEstado.setText(msg);
    }

    /**
     * Registra visualmente que un luchador se conecto al servidor.
     */
    /**
     * Registra visualmente un luchador conectado.
     * Cambia el slot de "Vacio" a "Conectado" (fondo verde, borde verde).
     * Actualiza el contador "Conectados: X / 6".
     * Se llama desde ControlVistaS cada vez que un cliente se conecta.
     */
    public void registrarLuchadorConectado(String nombre, double peso,
                                           int indice) {
        if (indice < 0 || indice >= MAX_LUCHADORES) return;
        panelesConectados.get(indice).conectar(nombre, peso);
        int conectados = 0;
        for (PanelLuchadorConectado p : panelesConectados) {
            if (p.estaConectado()) conectados++;
        }
        lblContador.setText("Conectados: " + conectados + " / " + MAX_LUCHADORES);
        actualizarEstado("Conectados: " + conectados + " / " + MAX_LUCHADORES
                         + " — Esperando luchadores...");
        mostrarMensaje("[CONEXION] " + nombre + " ("
                + String.format("%.1f", peso) + " kg) se ha conectado. ["
                + conectados + "/" + MAX_LUCHADORES + "]");

        // Forzar actualizacion visual de toda la grilla
        gridConectados.revalidate();
        gridConectados.repaint();
    }

    /**
     * Marca visualmente a los dos luchadores seleccionados para combatir.
     */
    public void mostrarSeleccionCombatientes(String nombre1, double peso1,
                                             String nombre2, double peso2) {
        for (PanelLuchadorConectado p : panelesConectados) {
            if (p.estaConectado()) {
                String n = p.getNombre();
                if (n.equals(nombre1) || n.equals(nombre2)) {
                    p.marcarSeleccionado();
                } else {
                    p.marcarEspera();
                }
            }
        }
        String info1 = nombre1 + " (" + String.format("%.1f", peso1) + " kg)";
        String info2 = nombre2 + " (" + String.format("%.1f", peso2) + " kg)";
        lblL1a.setText("LUCHADOR 1"); lblL1b.setText(info1);
        lblL2a.setText("LUCHADOR 2"); lblL2b.setText(info2);
        panelCombate.repaint();

        mostrarMensaje("[SELECCION] Combatientes elegidos al azar:");
        mostrarMensaje("   >> " + info1 + "  VS  " + info2);
        actualizarEstado("Seleccionados: " + nombre1 + " VS " + nombre2);

        gridConectados.revalidate();
        gridConectados.repaint();
    }

    public void mostrarLuchadorEnDohyo(String nombre, double peso,
                                       int indice) {
        String info = nombre + " (" + String.format("%.1f", peso) + " kg)";
        if (indice == 0) { lblL1a.setText("LUCHADOR 1"); lblL1b.setText(info); }
        else             { lblL2a.setText("LUCHADOR 2"); lblL2b.setText(info); }
        panelCombate.repaint();
    }

    public void mostrarInicioCombate(String n1, String n2) {
        combateEnCurso = true;
        actualizarEstado("COMBATE: " + n1 + " VS " + n2);
        setState(Frame.NORMAL);
        setAlwaysOnTop(true);
        toFront();
        requestFocus();
        setAlwaysOnTop(false);
    }

    /**
     * Muestra la imagen del kimarite ejecutado en el panel del dohyo.
     * Busca la imagen en Data/Servidor/Imagenes_tecnicas/{nombre}.png
     * El nombre se muestra en rojo (expulsado) o verde (resiste).
     */
    public void mostrarKimarite(String luchador, String kimarite,
                                boolean expulsado) {
        panelCombate.cargarKimarite(kimarite, expulsado);
        String res = expulsado ? "¡EXPULSADO!" : "El oponente resiste";
        actualizarEstado(luchador + " [" + kimarite + "] - " + res);
    }

    public void mostrarGanador(String nombre, int victorias) {
        lblGanador.setText("\uD83C\uDFC6 GANADOR: " + nombre
                           + "  |  Victorias: " + victorias);
        actualizarEstado("Combate finalizado. Ganador: " + nombre);

        for (PanelLuchadorConectado p : panelesConectados) {
            if (p.estaConectado() && p.getNombre().equals(nombre)) {
                p.marcarGanador();
            }
        }
        gridConectados.revalidate();
        gridConectados.repaint();
    }

    public void marcarLuchadorParticipo(String nombre) {
        for (PanelLuchadorConectado p : panelesConectados) {
            if (p.estaConectado() && p.getNombre().equals(nombre)) {
                p.marcarParticipo();
            }
        }
        gridConectados.revalidate();
        gridConectados.repaint();
    }

    public void resetearSeleccion() {
        for (PanelLuchadorConectado p : panelesConectados) {
            if (p.estaConectado() && !p.haParticipado()) {
                p.marcarConectado();
            }
        }
        lblL1a.setText("LUCHADOR 1"); lblL1b.setText("(Esperando...)");
        lblL2a.setText("LUCHADOR 2"); lblL2b.setText("(Esperando...)");
        lblGanador.setText(" ");
        panelCombate.limpiarKimarite();
        gridConectados.revalidate();
        gridConectados.repaint();
    }

    public void cerrar() {
        dispose();
        System.exit(0);
    }

    // ======================= Inner: PanelLuchadorConectado =======================

    private class PanelLuchadorConectado extends JPanel {

        private static final int ESTADO_VACIO       = 0;
        private static final int ESTADO_CONECTADO    = 1;
        private static final int ESTADO_SELECCIONADO = 2;
        private static final int ESTADO_ESPERA       = 3;
        private static final int ESTADO_GANADOR      = 4;
        private static final int ESTADO_PARTICIPO    = 5;

        private final int    slot;
        private final JLabel lblNombre;
        private final JLabel lblPeso;
        private final JLabel lblEstadoSlot;
        private String  nombre = "";
        private int     estado = ESTADO_VACIO;

        PanelLuchadorConectado(int slot) {
            this.slot = slot;
            setLayout(new BorderLayout(4, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_GRIS, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(50, 50, 60));

            lblNombre = new JLabel("Slot " + slot + " - Vacio",
                                    SwingConstants.LEFT);
            lblNombre.setFont(new Font("SansSerif", Font.BOLD, 12));
            lblNombre.setForeground(C_GRIS);

            lblPeso = new JLabel(" ", SwingConstants.LEFT);
            lblPeso.setFont(new Font("SansSerif", Font.PLAIN, 11));
            lblPeso.setForeground(new Color(180, 180, 180));

            lblEstadoSlot = new JLabel("\u23F3", SwingConstants.RIGHT);
            lblEstadoSlot.setFont(new Font("SansSerif", Font.BOLD, 11));
            lblEstadoSlot.setForeground(C_GRIS);

            JPanel centro = new JPanel(new GridLayout(2, 1));
            centro.setOpaque(false);
            centro.add(lblNombre);
            centro.add(lblPeso);

            add(centro, BorderLayout.CENTER);
            add(lblEstadoSlot, BorderLayout.EAST);
        }

        void conectar(String nombre, double peso) {
            this.nombre = nombre;
            this.estado = ESTADO_CONECTADO;
            lblNombre.setText(nombre);
            lblNombre.setForeground(C_BLANC);
            lblPeso.setText(String.format("%.1f kg", peso));
            lblEstadoSlot.setText("\u2705 Conectado");
            lblEstadoSlot.setForeground(C_VERDE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_VERDE, 2, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(40, 60, 45));
            revalidate();
            repaint();
        }

        void marcarSeleccionado() {
            estado = ESTADO_SELECCIONADO;
            lblEstadoSlot.setText("\u2694\uFE0F Combatiendo");
            lblEstadoSlot.setForeground(C_NARANJA);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_NARANJA, 2, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(70, 55, 25));
            revalidate();
            repaint();
        }

        void marcarEspera() {
            if (estado == ESTADO_PARTICIPO) return;
            estado = ESTADO_ESPERA;
            lblEstadoSlot.setText("\u231B En espera");
            lblEstadoSlot.setForeground(new Color(170, 170, 170));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_GRIS, 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(50, 50, 60));
            revalidate();
            repaint();
        }

        void marcarConectado() {
            estado = ESTADO_CONECTADO;
            lblEstadoSlot.setText("\u2705 Conectado");
            lblEstadoSlot.setForeground(C_VERDE);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_VERDE, 2, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(40, 60, 45));
            revalidate();
            repaint();
        }

        void marcarGanador() {
            estado = ESTADO_GANADOR;
            lblEstadoSlot.setText("\uD83C\uDFC6 Ganador");
            lblEstadoSlot.setForeground(C_DORADO);
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(C_DORADO, 2, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(60, 55, 20));
            revalidate();
            repaint();
        }

        void marcarParticipo() {
            estado = ESTADO_PARTICIPO;
            lblEstadoSlot.setText("\u2714 Ya combatio");
            lblEstadoSlot.setForeground(new Color(130, 130, 130));
            setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(90, 90, 90), 1, true),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(new Color(45, 45, 50));
            lblNombre.setForeground(new Color(160, 160, 160));
            revalidate();
            repaint();
        }

        boolean estaConectado() { return estado != ESTADO_VACIO; }
        boolean haParticipado() { return estado == ESTADO_PARTICIPO; }
        String  getNombre()     { return nombre; }
    }

    // ======================= Inner: PanelCombate =======================

    private class PanelCombate extends JPanel {
        private final Image imgFondo, imgL1, imgL2;
        private Image  imgKimarite = null;
        private String nomKimarite = "";
        private Color  colKimarite = Color.WHITE;

        public PanelCombate() {
            setOpaque(true);
            imgFondo = cargarImagen(REC + "Dohyo.png", -1, -1);
            imgL1    = cargarImagen(REC + "Luchador1.png", -1, -1);
            imgL2    = cargarImagen(REC + "Luchador2.png", -1, -1);
        }

        public void cargarKimarite(String nombre, boolean expulsado) {
            nomKimarite = nombre;
            colKimarite = expulsado ? new Color(255, 80, 80)
                                    : new Color(100, 240, 130);
            imgKimarite = null;
            for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
                File f = new File(KTEC + nombre + ext);
                if (f.exists()) {
                    imgKimarite = cargarImagen(KTEC + nombre + ext, -1, -1);
                    break;
                }
            }
            repaint();
        }

        public void limpiarKimarite() {
            imgKimarite = null;
            nomKimarite = "";
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            int w = getWidth(), h = getHeight();
            if (imgFondo != null) g2.drawImage(imgFondo, 0, 0, w, h, null);
            else {
                g2.setColor(new Color(60, 45, 30));
                g2.fillRect(0, 0, w, h);
            }
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRect(0, 0, w, h);
            pintarLuchador(g2, imgL1, w, h, false);
            pintarLuchador(g2, imgL2, w, h, true);
            if (imgKimarite != null) pintarKimarite(g2, w, h);
            g2.dispose();
        }

        private void pintarLuchador(Graphics2D g2, Image img, int w, int h,
                                    boolean der) {
            if (img == null) return;
            int m = w / 2;
            int maxW = (int)(m * 0.55), maxH = (int)(h * 0.68);
            double esc = Math.min((double) maxW / img.getWidth(null),
                                  (double) maxH / img.getHeight(null));
            int rw = (int)(img.getWidth(null) * esc);
            int rh = (int)(img.getHeight(null) * esc);
            int cx = der ? (m + m / 2) : (m / 2);
            g2.drawImage(img, cx - rw / 2, (int)(h * 0.15), rw, rh, null);
        }

        private void pintarKimarite(Graphics2D g2, int w, int h) {
            int maxW = (int)(w * 0.28), maxH = (int)(h * 0.38);
            int origW = imgKimarite.getWidth(null);
            int origH = imgKimarite.getHeight(null);
            if (origW <= 0 || origH <= 0) return;
            double esc = Math.min((double) maxW / origW,
                                  (double) maxH / origH);
            int rw = (int)(origW * esc), rh = (int)(origH * esc);
            int kx = w / 2 - rw / 2, ky = (int)(h * 0.32) - rh / 2;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect(kx - 12, ky - 10, rw + 24, rh + 30, 14, 14);
            g2.drawImage(imgKimarite, kx, ky, rw, rh, null);
            g2.setFont(new Font("Serif", Font.BOLD, 13));
            g2.setColor(colKimarite);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(nomKimarite,
                    w / 2 - fm.stringWidth(nomKimarite) / 2, ky + rh + 18);
        }
    }
}
