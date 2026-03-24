//Clase "principal" del anterior proyecto pero que tiene revueltas responsbilidades de control vista y de socket
package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.modelo.cliente.CargadorPropiedades;
import co.edu.udistrital.sumo.modelo.cliente.ConexionCliente;
import co.edu.udistrital.sumo.vista.cliente.VistaCliente;

import java.io.IOException;
import java.util.List;

/**
 * Controlador del lado del cliente en la arquitectura MVC del Combate de Sumo.
 *
 * Propósito: Mediar entre la {@link VistaCliente} y la lógica de conexión
 * al servidor vía {@link ConexionCliente}. Sus responsabilidades son:
 * - Obtener la ruta del properties desde la Vista (que gestiona el JFileChooser).
 * - Cargar los kimarites usando {@link CargadorPropiedades}.
 * - Validar los datos ingresados por el usuario.
 * - Conectar al servidor vía {@link ConexionCliente} y enviar los datos.
 * - Esperar la respuesta del combate y notificar el resultado a la Vista.
 * Las acciones de botones están desacopladas en {@link AccionCargarKimarites} y {@link AccionConectar}.
 * Se comunica con: {@link VistaCliente} (vista), {@link CargadorPropiedades}(modelo - conexión properties), {@link ConexionCliente} (modelo - socket).
 * Principio SOLID:
 * S — única responsabilidad: coordinar el flujo del cliente.
 *
 * PROHIBIDO en esta clase: JFileChooser, ServerSocket, componentes Swing directos.
 *
 * @author Grupo Programación avanzada
 * @version 2.6
 * @see VistaCliente
 * @see ConexionCliente
 * @see CargadorPropiedades
 */
public class ControladorCliente {

    //Dirección IP del servidor de sumo
    private static final String HOST_SERVIDOR = "localhost";

    //Puerto en el que escucha el servidor de sumo
    private static final int PUERTO_SERVIDOR = 9999;

    //Vista del cliente (formulario del luchador)
    private final VistaCliente vista;

    //Conexión al archivo de propiedades de kimarites (modelo)
    private final CargadorPropiedades cargadorPropiedades;

    /**
     * Construye el controlador del cliente, inicializa la vista y registra las acciones desacopladas en los botones.
     */
    public ControladorCliente() {
        this.cargadorPropiedades = new CargadorPropiedades();
        this.vista = new VistaCliente();
        registrarAcciones();
        this.vista.setVisible(true);
    }

    /**
     * Método estático de entrada invocado desde {@link LauncherCliente}.
     * Crea la única instancia del controlador sin que el Launcher cree objetos.
     */
    public static void iniciar() {
        new ControladorCliente();
    }

    //Registra las acciones desacopladas en los botones de la vista.
    //Este método SOLO asigna listeners — no contiene lógica de negocio.
    private void registrarAcciones() {
        vista.getBtnCargarKimarites().addActionListener(new AccionCargarKimarites(this));
        vista.getBtnConectar().addActionListener(new AccionConectar(this));
    }

    /**
     * Solicita la ruta del properties a la Vista (que gestiona el JFileChooser),
     * carga los kimarites con {@link CargadorPropiedades} y los muestra en la lista.
     * Invocado desde {@link AccionCargarKimarites}.
     */
    public void cargarKimarites() {
        //La Vista gestiona el JFileChooser y retorna solo la ruta (Respeta el Open/Close)
        String ruta = vista.seleccionarRutaProperties();
        if (ruta == null) {
            vista.mostrarMensaje("No se seleccionó ningún archivo.");
            return;
        }

        List<String> nombres;
        //Excepciónes
        try {
            nombres = cargadorPropiedades.cargarNombres(ruta);
        } catch (IOException e) {
            vista.mostrarMensaje("Error al leer el archivo: " + e.getMessage());
            return;
        }

        if (nombres.isEmpty()) {
            vista.mostrarMensaje("El archivo no contiene kimarites válidos.");
            return;
        }

        vista.cargarListaKimarites(nombres);
        vista.mostrarEstado("Kimarites cargados: " + nombres.size() + " tecnicas disponibles");
    }

    /**
     * Valida los datos del formulario y conecta al servidor vía {@link ConexionCliente}.
     * La conexión se ejecuta en un hilo de fondo para no bloquear el hilo de Swing.
     * Invocado desde {@link AccionConectar}.
     */
    public void conectarAlServidor() {
        //Validaciones de campos
        String nombre = vista.getNombreLuchador().trim();
        if (nombre.isEmpty()) {
            vista.mostrarMensaje("Debe ingresar el nombre del luchador.");
            return;
        }

        double peso;
        try {
            peso = Double.parseDouble(vista.getPesoLuchador().trim());
            if (peso <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            vista.mostrarMensaje("El peso debe ser un numero positivo valido.");
            return;
        }

        List<String> kimaritesSeleccionados = vista.getKimaritesSeleccionados();
        if (kimaritesSeleccionados.isEmpty()) {
            vista.mostrarMensaje("Debe seleccionar al menos un kimarite.");
            return;
        }

        //Formatear mensaje para el servidor: "nombre|peso|k1,k2,k3..."
        String mensajeServidor = nombre + "|" + peso + "|" + String.join(",", kimaritesSeleccionados); //Uso del join

        vista.setBtnConectarHabilitado(false);
        vista.mostrarEstado("Conectando al servidor...");

        //Conectar en hilo aparte para no bloquear el EDT de Swing
        Thread hiloConexion = new Thread(
            () -> ejecutarCombate(mensajeServidor), "HiloConexionCliente");
        hiloConexion.setDaemon(true);
        hiloConexion.start();
    }

    /**
     * Ejecuta el flujo completo: conectar, enviar datos, esperar resultado y cerrar.
     * Se ejecuta en el hilo de fondo iniciado por {@link conectarAlServidor()}.
     *
     * El orden es importante: primero se muestra el resultado al usuario
     * y se espera a que presione OK ({@code invokeAndWait}), y solo después
     * se envía "LISTO" al servidor. Esto garantiza que el servidor no cierre
     * su ventana hasta que ambos clientes hayan confirmado con el OK.
     *
     * @param mensajeServidor datos del luchador formateados para el servidor
     */
    private void ejecutarCombate(String mensajeServidor) {
        ConexionCliente conexion = new ConexionCliente(HOST_SERVIDOR, PUERTO_SERVIDOR);
        try {
            conexion.conectar();
            actualizarVista("Conectado. Esperando al oponente...");

            // Enviar datos del luchador al servidor
            conexion.enviar(mensajeServidor);

            // Esperar resultado del combate (bloqueante hasta que el servidor responda)
            String resultado = conexion.recibirRespuesta();

            boolean gano = "GANASTE!!!".equals(resultado);
            actualizarVista(gano ? "¡GANASTE EL COMBATE!" : "Perdiste el combate..D;");

            // invokeAndWait bloquea este hilo hasta que el usuario presione OK
            // y la vista se cierre — solo entonces se continúa con el LISTO
            try {
                javax.swing.SwingUtilities.invokeAndWait(() -> vista.mostrarResultadoCombate(gano));
            } catch (java.lang.reflect.InvocationTargetException ex) {
                // Si la vista lanzó una excepción, la ignoramos y seguimos
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            // El usuario ya presionó OK — ahora sí avisamos al servidor
            conexion.enviar("LISTO");

        } catch (IOException e) {
            actualizarVista("Error de conexion: " + e.getMessage());
        } finally {
            try { conexion.cerrar(); } catch (IOException ignored) {}
        }
    }

    // Actualiza el estado de la vista desde un hilo de fondo (respeta el EDT)
    private void actualizarVista(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarEstado(mensaje));
    }
}
