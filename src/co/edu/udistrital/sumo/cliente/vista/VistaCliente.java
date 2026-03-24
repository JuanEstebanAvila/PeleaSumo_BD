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
 * Vista del cliente.
 *
 * Recursos en Data/Recursos/:
 *   Fondo_Japones.png, Luchador1.png, Luchador2.png, Logo_Sumo.png,
 *   japones.png, samurai.png, flor-de-cerezo.png,
 *   gato-chino-de-la-suerte.png, japon.png
 *
 * @author Grupo Programación avanzada
 * @version 2.8
 */
public class VistaCliente extends JFrame {

    //Rutas
    private static final String REC       = "Data/Recursos/";
    // Archivo bandera para alternar imagen entre procesos JVM */
    private static final String BANDERA   = "Data/luchador_flag.tmp";

    //Colores 
    private static final Color C_ROJO_PANEL = new Color(237, 85, 90);
    private static final Color C_AZUL       = new Color(70, 130, 210);
    private static final Color C_TEXTO      = new Color(25, 15, 10);
    private static final Color C_CAMPO      = new Color(250, 200, 200);
    private static final Color C_LISTA      = new Color(252, 215, 215);
    private static final Color C_BLANCO     = Color.WHITE;

    //Componentes 
    private final JTextField           txtNombre;
    private final JTextField           txtPeso;
    private final DefaultListModel<String> modeloLista;
    private final JList<String>        listKimarites;
    private final JButton              btnCargarKimarites;
    private final JButton              btnConectar;
    private final JLabel               lblEstado;
    private final JFileChooser         fileChooser;

    private final Image  imgFondo;
    /** Ruta del luchador determinada por la bandera de proceso */
    private final String rutaLuchador;

    public VistaCliente() {
        super("Combate de Sumo - Registro del Luchador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Alternancia de imagen entre procesos JVM (Para que la imagen del luchador sea diferente en la segunda ejecución de la vista del cliente)
        rutaLuchador = resolverRutaLuchador();

        imgFondo = cargarImagen(REC + "Fondo_Japones.png", -1, -1);

        txtNombre          = crearCampo();
        txtPeso            = crearCampo();
        modeloLista        = new DefaultListModel<>();
        listKimarites      = crearLista();
        btnCargarKimarites = crearBotonAzul("📂 Cargar Kimarites");
        btnConectar        = crearBotonAzulGrande("👊 ENTRAR AL DOHYO 👊");
        lblEstado          = crearLblEstado();

        fileChooser = new JFileChooser(new File("Data/"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de propiedades (*.properties)", "properties"));
        fileChooser.setDialogTitle("Seleccionar archivo de kimarites");

        construirUI();
    }

    /**
     * Determina qué imagen de luchador usar según el archivo bandera.
     * Si el archivo NO existe → primer cliente → usa Luchador1.png y CREA el archivo.
     * Si el archivo SÍ existe → segundo cliente → usa Luchador2.png y BORRA el archivo.
     */
    private String resolverRutaLuchador() {
        File bandera = new File(BANDERA);
        if (!bandera.exists()) {
            // Primer cliente
            try { bandera.createNewFile(); } catch (IOException ignored) {}
            return REC + "Luchador1.png";
        } else {
            // Segundo cliente
            bandera.delete();
            return REC + "Luchador2.png";
        }
    }

    // Layout 

    private void construirUI() {
        JPanel raiz = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                if (imgFondo != null)
                    g2.drawImage(imgFondo, 0, 0, getWidth(), getHeight(), this);
                else {
                    g2.setColor(new Color(245, 210, 200));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                // Silueta oscura sobre el fondo
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        raiz.setOpaque(true);
        setContentPane(raiz);

        raiz.add(construirHeader(), BorderLayout.NORTH);
        raiz.add(construirCentro(), BorderLayout.CENTER);
        raiz.add(construirEstado(), BorderLayout.SOUTH);
    }

    /**
     * Panel superior de la ventana 
     */
    private JPanel construirHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_ROJO_PANEL);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Panel central con BoxLayout: [icono] [espacio] [titulo] [espacio] [bandera]
        JPanel pContenido = new JPanel();
        pContenido.setLayout(new BoxLayout(pContenido, BoxLayout.X_AXIS));
        pContenido.setOpaque(false);

        // Ícono sumo
        JLabel lblIcono = new JLabel();
        lblIcono.setIcon(escalarIcono(REC + "Logo_Sumo.png", 58, 58));

        // Separador mínimo entre ícono y texto
        pContenido.add(lblIcono);
        pContenido.add(Box.createRigidArea(new Dimension(12, 0)));

        // Título
        JLabel lblTitulo = new JLabel("¡COMBATE DE SUMO 🏋️!");
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 36));
        lblTitulo.setForeground(C_BLANCO);
        pContenido.add(lblTitulo);

        pContenido.add(Box.createRigidArea(new Dimension(12, 0)));

        // Bandera
        JLabel lblBandera = new JLabel();
        lblBandera.setIcon(escalarIcono(REC + "japones.png", 58, 58));
        pContenido.add(lblBandera);

        // Envolver en un panel centrador
        JPanel pCentrador = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pCentrador.setOpaque(false);
        pCentrador.add(pContenido);

        p.add(pCentrador, BorderLayout.CENTER);
        return p;
    }

    /** Centro: formulario (izquierda) + luchador (derecha) */
    private JPanel construirCentro() {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(16, 28, 12, 28));
        p.add(construirFormulario());
        p.add(construirPanelLuchador());
        return p;
    }

    /** Panel rojo izquierdo */
    private JPanel construirFormulario() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(C_ROJO_PANEL);
        p.setBorder(new EmptyBorder(18, 22, 16, 22));

        JPanel pCampos = new JPanel(new GridBagLayout());
        pCampos.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 8);

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        pCampos.add(lblCampo("Nombre del Rikishi:"), g);
        g.gridx = 1; g.weightx = 0;
        pCampos.add(new JLabel(escalarIcono(REC + "samurai.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        pCampos.add(txtNombre, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        pCampos.add(lblCampo("Peso(kg):"), g);
        g.gridx = 1; g.weightx = 0;
        pCampos.add(new JLabel(escalarIcono(REC + "flor-de-cerezo.png", 36, 36)), g);
        g.gridx = 2; g.weightx = 1;
        pCampos.add(txtPeso, g);

        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; g.insets = new Insets(14, 0, 2, 4);
        pCampos.add(new JLabel(escalarIcono(REC + "gato-chino-de-la-suerte.png", 42, 42)), g);
        g.gridx = 1; g.gridwidth = 1; g.weightx = 1; g.insets = new Insets(14, 0, 2, 4);
        pCampos.add(btnCargarKimarites, g);
        g.gridx = 2; g.weightx = 0; g.insets = new Insets(14, 4, 2, 0);
        pCampos.add(new JLabel(escalarIcono(REC + "japon.png", 42, 42)), g);

        p.add(pCampos, BorderLayout.NORTH);

        JPanel pLista = new JPanel(new BorderLayout(0, 5));
        pLista.setOpaque(false);
        JLabel lblTec = new JLabel("Tecnicas disponibles");
        lblTec.setFont(new Font("Serif", Font.BOLD, 16));
        lblTec.setForeground(C_BLANCO);
        pLista.add(lblTec, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(listKimarites);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 60, 60), 2),
            "Seleccionar Kimarites (Ctrl + clic)",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Serif", Font.BOLD, 12), new Color(60, 20, 20)));
        scroll.setBackground(C_LISTA);
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

    /** Panel derecho con círculo rojo y luchador */
    private JPanel construirPanelLuchador() {
        return new JPanel() {
            final Image imgLuchador = cargarImagen(rutaLuchador, -1, -1);
            { setOpaque(false); }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRect(0, 0, w, h);

                int r  = (int)(Math.min(w, h) * 0.80);
                int ox = w / 2 - r / 2;
                int oy = h / 2 - r / 2 + 20;
                g2.setColor(new Color(200, 40, 40, 210));
                g2.fillOval(ox, oy, r, r);

                if (imgLuchador != null) {
                    int maxW = (int)(w * 0.82);
                    int maxH = (int)(h * 0.88);
                    double esc = Math.min(
                        (double) maxW / imgLuchador.getWidth(null),
                        (double) maxH / imgLuchador.getHeight(null));
                    int rw = (int)(imgLuchador.getWidth(null) * esc);
                    int rh = (int)(imgLuchador.getHeight(null) * esc);
                    int lx = w / 2 - rw / 2;
                    int ly = (oy + r / 2) - rh / 2 + 10;
                    g2.drawImage(imgLuchador, lx, ly, rw, rh, null);
                }
                g2.dispose();
            }
        };
    }

    /** Barra azul inferior */
    private JPanel construirEstado() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_AZUL);
        p.setBorder(new EmptyBorder(11, 22, 11, 22));
        p.add(lblEstado, BorderLayout.CENTER);
        return p;
    }

    // Fábricas 

    private JTextField crearCampo() {
        JTextField t = new JTextField();
        t.setBackground(C_CAMPO);
        t.setForeground(C_TEXTO);
        t.setFont(new Font("Serif", Font.PLAIN, 15));
        t.setCaretColor(C_TEXTO);
        t.setPreferredSize(new Dimension(220, 38));
        t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 100, 100), 1),
            new EmptyBorder(8, 10, 8, 10)));
        return t;
    }

    private JList<String> crearLista() {
        JList<String> l = new JList<>(modeloLista);
        l.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        l.setBackground(C_LISTA);
        l.setForeground(C_TEXTO);
        l.setFont(new Font("Serif", Font.PLAIN, 13));
        l.setSelectionBackground(new Color(70, 130, 210));
        l.setSelectionForeground(C_BLANCO);
        l.setFixedCellHeight(27);
        l.setBorder(new EmptyBorder(4, 8, 4, 8));
        return l;
    }

    private JButton crearBotonAzul(String txt) {
        JButton b = new JButton(txt);
        b.setBackground(C_AZUL);
        b.setForeground(C_BLANCO);
        b.setFont(new Font("Serif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        return b;
    }

    private JButton crearBotonAzulGrande(String txt) {
        JButton b = crearBotonAzul(txt);
        b.setFont(new Font("Serif", Font.BOLD, 18));
        b.setBorder(new EmptyBorder(14, 0, 14, 0));
        return b;
    }

    private JLabel lblCampo(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Serif", Font.BOLD, 15));
        l.setForeground(C_BLANCO);
        return l;
    }

    private JLabel crearLblEstado() {
        JLabel l = new JLabel("*MENSAJE CON ESTADO ACTUAL E INSTRUCCIONES*",
                               SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, 14));
        l.setForeground(C_BLANCO);
        return l;
    }

    // Carga de imágenes 

    private Image cargarImagen(String ruta, int ancho, int alto) {
        try {
            File f = new File(ruta);
            if (!f.exists()) return null;
            ImageIcon raw = new ImageIcon(f.getAbsolutePath());
            if (ancho == -1) return raw.getImage();
            return raw.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        } catch (Exception e) { return null; }
    }

    private ImageIcon escalarIcono(String ruta, int ancho, int alto) {
        Image img = cargarImagen(ruta, ancho, alto);
        return img != null ? new ImageIcon(img) : new ImageIcon();
    }

    // API pública 

    public String seleccionarRutaProperties() {
        int res = fileChooser.showOpenDialog(this);
        return res == JFileChooser.APPROVE_OPTION
            ? fileChooser.getSelectedFile().getAbsolutePath()
            : null;
    }

    public void cargarListaKimarites(List<String> nombres) {
        modeloLista.clear();
        nombres.forEach(modeloLista::addElement);
    }

    public List<String> getKimaritesSeleccionados() {
        return new ArrayList<>(listKimarites.getSelectedValuesList());
    }

    public String getNombreLuchador()  { return txtNombre.getText(); }
    public String getPesoLuchador()    { return txtPeso.getText(); }

    public void mostrarEstado(String msg) { lblEstado.setText(msg); }

    public void mostrarMensaje(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Aviso",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarResultadoCombate(boolean gano) {
        String titulo = gano ? " ¡🏆GANASTE!"  : "✖️ Perdiste";
        String msg    = gano
            ? "Felicitaciones! Tu rikishi gano el combate."
            : "Tu rikishi fue expulsado del dohyo.";
        JOptionPane.showMessageDialog(this, msg, titulo,
            gano ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        dispose();
    }

    public void setBtnConectarHabilitado(boolean h) { btnConectar.setEnabled(h); }
    public JButton getBtnCargarKimarites()          { return btnCargarKimarites; }
    public JButton getBtnConectar()                 { return btnConectar; }
}
