package co.edu.udistrital.sumo.cliente.vista;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista del cliente: formulario de registro del luchador (MVC - Vista).
 * 
 * Esta clase SOLO se encarga de la presentacion grafica.
 * No contiene logica de negocio, ni sockets, ni SQL.
 * Todos los eventos se manejan en ControlVista (ActionListener).
 * 
 * Componentes principales:
 *   - txtNombre: campo de texto para el nombre del rikishi
 *   - txtPeso: campo de texto para el peso en kg
 *   - listKimarites: JList multi-seleccion con las tecnicas cargadas
 *   - btnCargar: abre JFileChooser para seleccionar kimarites.properties
 *   - btnConectar: envia los datos al servidor y espera resultado
 *   - fileChooser: JFileChooser configurado para archivos .properties
 * 
 * Resultado del combate: se muestra con JOptionPane (permitido por enunciado).
 * Maneja 3 estados: GANASTE, PERDISTE y SIN_COMBATE con mensajes distintos.
 * Recursos en Data/Cliente/Recursos/.
 * No contiene logica de negocio ni objetos del modelo.
 *
 * @author Grupo Programacion Avanzada
 */
public class VistaCliente extends JFrame {

    private static final String REC     = "Data/Cliente/Recursos/";
    private static final String BANDERA = "Data/Cliente/luchador_flag.tmp";

    private static final Color C_ROJO  = new Color(237, 85, 90);
    private static final Color C_AZUL  = new Color(70, 130, 210);
    private static final Color C_TEXTO = new Color(25, 15, 10);
    private static final Color C_CAMPO = new Color(250, 200, 200);
    private static final Color C_LISTA = new Color(252, 215, 215);
    private static final Color C_BLANC = Color.WHITE;

    private final JTextField                txtNombre;
    private final JTextField                txtPeso;
    private final DefaultListModel<String>  modeloLista;
    private final JList<String>             listKimarites;
    private final JButton                   btnCargar;
    private final JButton                   btnConectar;
    private final JLabel                    lblEstado;
    private final JFileChooser              fileChooser;
    private final Image                     imgFondo;
    private final String                    rutaLuchador;

    public VistaCliente() {
        super("Combate de Sumo - Registro del Luchador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        rutaLuchador = resolverImagenLuchador();
        imgFondo     = cargarImagen(REC + "Fondo_Japones.png", -1, -1);

        txtNombre     = crearCampo();
        txtPeso       = crearCampo();
        modeloLista   = new DefaultListModel<>();
        listKimarites = crearLista();
        btnCargar     = crearBoton("📂 Cargar Kimarites", 14);
        btnConectar   = crearBoton("👊 ENTRAR AL DOHYO 👊", 18);
        lblEstado     = crearLblEstado();

        fileChooser = new JFileChooser(new File("Data/Cliente/"));
        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "Archivo de propiedades (*.properties)", "properties"));
        fileChooser.setDialogTitle("Seleccionar kimarites.properties");

        construirUI();
    }

    /**
     * Alterna la imagen del luchador entre Luchador1 y Luchador2
     * usando un archivo temporal como bandera.
     * Primer cliente -> Luchador1.png, segundo -> Luchador2.png.
     */
    private String resolverImagenLuchador() {
        File bandera = new File(BANDERA);
        if (!bandera.exists()) {
            try { bandera.createNewFile(); } catch (IOException ignored) {}
            return REC + "Luchador1.png";
        } else {
            bandera.delete();
            return REC + "Luchador2.png";
        }
    }

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (imgFondo != null)
                    g2.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), this);
                else {
                    g2.setColor(new Color(245, 210, 200));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        setContentPane(raiz);
        raiz.add(construirHeader(), BorderLayout.NORTH);
        raiz.add(construirCentro(), BorderLayout.CENTER);
        raiz.add(construirBarraEstado(), BorderLayout.SOUTH);
    }

    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_ROJO);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.X_AXIS));
        contenido.setOpaque(false);
        JLabel icono = new JLabel(escalarIcono(REC + "Logo_Sumo.png", 58, 58));
        JLabel titulo = new JLabel("¡COMBATE DE SUMO 🏋\uFE0F!");
        titulo.setFont(new Font("Serif", Font.BOLD, 36));
        titulo.setForeground(C_BLANC);
        JLabel bandera = new JLabel(escalarIcono(REC + "japones.png", 58, 58));
        contenido.add(icono);
        contenido.add(Box.createRigidArea(new Dimension(12, 0)));
        contenido.add(titulo);
        contenido.add(Box.createRigidArea(new Dimension(12, 0)));
        contenido.add(bandera);
        JPanel centrador = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centrador.setOpaque(false);
        centrador.add(contenido);
        p.add(centrador, BorderLayout.CENTER);
        return p;
    }

    private JPanel construirCentro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 28, 12, 28));
        p.add(construirFormulario());
        p.add(construirPanelLuchador());
        return p;
    }

    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(C_ROJO);
        p.setBorder(new EmptyBorder(18, 22, 16, 22));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 8);

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        campos.add(lbl("Nombre del Rikishi:"), g);
        g.gridx = 1;
        campos.add(new JLabel(escalarIcono(REC + "samurai.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        campos.add(txtNombre, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        campos.add(lbl("Peso (kg):"), g);
        g.gridx = 1;
        campos.add(new JLabel(escalarIcono(
                REC + "flor-de-cerezo.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        campos.add(txtPeso, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 1;
        g.insets = new Insets(14, 0, 2, 4);
        campos.add(new JLabel(escalarIcono(
                REC + "gato-chino-de-la-suerte.png", 42, 42)), g);
        g.gridx = 1; g.gridwidth = 1; g.weightx = 1;
        g.insets = new Insets(14, 0, 2, 4);
        campos.add(btnCargar, g);
        g.gridx = 2; g.weightx = 0;
        g.insets = new Insets(14, 4, 2, 0);
        campos.add(new JLabel(escalarIcono(REC + "japon.png", 42, 42)), g);

        p.add(campos, BorderLayout.NORTH);

        JPanel pLista = new JPanel(new BorderLayout(0, 5));
        pLista.setOpaque(false);
        JLabel lblTec = new JLabel(
                "Tecnicas disponibles (Ctrl+clic para seleccionar)");
        lblTec.setFont(new Font("Serif", Font.BOLD, 14));
        lblTec.setForeground(C_BLANC);
        pLista.add(lblTec, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(listKimarites);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 60, 60), 2),
            "Seleccionar Kimarites",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Serif", Font.BOLD, 12), new Color(60, 20, 20)));
        scroll.getViewport().setBackground(C_LISTA);
        pLista.add(scroll, BorderLayout.CENTER);
        p.add(pLista, BorderLayout.CENTER);

        JPanel pBot = new JPanel(new BorderLayout());
        pBot.setOpaque(false);
        pBot.setBorder(new EmptyBorder(10, 0, 0, 0));
        pBot.add(btnConectar, BorderLayout.CENTER);
        p.add(pBot, BorderLayout.SOUTH);
        return p;
    }

    private JPanel construirPanelLuchador() {
        return new JPanel() {
            final Image imgL = cargarImagen(rutaLuchador, -1, -1);
            { setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, w, h);
                int r = (int)(Math.min(w, h) * 0.80);
                int ox = w / 2 - r / 2, oy = h / 2 - r / 2 + 20;
                g2.setColor(new Color(200, 40, 40, 210));
                g2.fillOval(ox, oy, r, r);
                if (imgL != null) {
                    int maxW = (int)(w * 0.82), maxH = (int)(h * 0.88);
                    double esc = Math.min(
                            (double) maxW / imgL.getWidth(null),
                            (double) maxH / imgL.getHeight(null));
                    int rw = (int)(imgL.getWidth(null) * esc);
                    int rh = (int)(imgL.getHeight(null) * esc);
                    g2.drawImage(imgL, w / 2 - rw / 2,
                            (oy + r / 2) - rh / 2 + 10, rw, rh, null);
                }
                g2.dispose();
            }
        };
    }

    private JPanel construirBarraEstado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_AZUL);
        p.setBorder(new EmptyBorder(11, 22, 11, 22));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // --- Fabricas de componentes ---

    private JTextField crearCampo() {
        JTextField t = new JTextField();
        t.setBackground(C_CAMPO); t.setForeground(C_TEXTO);
        t.setFont(new Font("Serif", Font.PLAIN, 15));
        t.setPreferredSize(new Dimension(220, 38));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 100, 100), 1),
            new EmptyBorder(8, 10, 8, 10)));
        return t;
    }

    private JList<String> crearLista() {
        JList<String> l = new JList<>(modeloLista);
        l.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        l.setBackground(C_LISTA); l.setForeground(C_TEXTO);
        l.setFont(new Font("Serif", Font.PLAIN, 13));
        l.setSelectionBackground(C_AZUL);
        l.setSelectionForeground(C_BLANC);
        l.setFixedCellHeight(27);
        return l;
    }

    private JButton crearBoton(String txt, int tam) {
        JButton b = new JButton(txt);
        b.setBackground(C_AZUL); b.setForeground(C_BLANC);
        b.setFont(new Font("Serif", Font.BOLD, tam));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 18, 12, 18));
        return b;
    }

    private JLabel lbl(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Serif", Font.BOLD, 15));
        l.setForeground(C_BLANC);
        return l;
    }

    private JLabel crearLblEstado() {
        JLabel l = new JLabel(
                "Seleccione el archivo de kimarites para comenzar.",
                SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 14));
        l.setForeground(C_BLANC);
        return l;
    }

    // --- Carga de imagenes ---

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

    // --- API publica ---

    public String seleccionarProperties() {
        int r = fileChooser.showOpenDialog(this);
        return r == JFileChooser.APPROVE_OPTION
            ? fileChooser.getSelectedFile().getAbsolutePath() : null;
    }

    public void cargarKimarites(List<String> kimarites) {
        modeloLista.clear();
        kimarites.forEach(modeloLista::addElement);
    }

    public List<String> getKimaritesSeleccionados() {
        return new ArrayList<>(listKimarites.getSelectedValuesList());
    }

    public String getNombre() { return txtNombre.getText(); }
    public String getPeso()   { return txtPeso.getText(); }

    public void mostrarEstado(String msg) { lblEstado.setText(msg); }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra el resultado del combate segun el estado recibido.
     * Maneja 3 estados: GANASTE, PERDISTE, SIN_COMBATE.
     * @param resultado texto recibido del servidor
     */
    /**
     * Muestra el resultado del combate con JOptionPane y cierra la ventana.
     * 
     * GANASTE -> JOptionPane.INFORMATION_MESSAGE + mensaje de felicitacion
     * PERDISTE -> JOptionPane.WARNING_MESSAGE + mensaje de derrota
     * SIN_COMBATE -> JOptionPane.INFORMATION_MESSAGE + no fue seleccionado
     * 
     * Despues de mostrar el resultado, se llama dispose() para cerrar
     * la ventana, lo que permite al ControlSocket enviar 'LISTO' al servidor.
     * 
     * @param resultado texto recibido del servidor
     */
    public void mostrarResultado(String resultado) {
        String titulo;
        String msg;
        int tipo;

        if ("GANASTE".equals(resultado)) {
            titulo = "¡GANASTE!";
            msg    = "¡Felicitaciones! Tu rikishi gano el combate.";
            tipo   = JOptionPane.INFORMATION_MESSAGE;
        } else if ("PERDISTE".equals(resultado)) {
            titulo = "Perdiste";
            msg    = "Tu rikishi fue expulsado del dohyo.";
            tipo   = JOptionPane.WARNING_MESSAGE;
        } else {
            // SIN_COMBATE u otro
            titulo = "Sin combate";
            msg    = "Tu rikishi no fue seleccionado para combatir en este torneo.";
            tipo   = JOptionPane.INFORMATION_MESSAGE;
        }

        JOptionPane.showMessageDialog(this, msg, titulo, tipo);
        dispose();
    }

    public void setBtnConectarHabilitado(boolean h) {
        btnConectar.setEnabled(h);
    }

    public JButton getBtnCargar()   { return btnCargar; }
    public JButton getBtnConectar() { return btnConectar; }
}
