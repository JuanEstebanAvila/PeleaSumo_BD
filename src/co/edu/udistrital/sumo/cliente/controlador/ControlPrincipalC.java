package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionCliente;
import co.edu.udistrital.sumo.cliente.modelo.ConexionProperties;
import co.edu.udistrital.sumo.cliente.vista.VistaCliente;

import java.io.IOException;
import java.util.List;

/**
 * Controlador principal del lado del cliente en la arquitectura MVC.
 *
 * Responsabilidades:
 * - Obtener la ruta del .properties desde la Vista.
 * - Cargar los kimarites usando ConexionProperties.
 * - Validar los datos ingresados por el usuario.
 * - Conectar al servidor via ConexionCliente y enviar los datos.
 * - Esperar la respuesta del combate y notificar el resultado a la Vista.
 *
 * Principio SOLID — S: única responsabilidad: coordinar el flujo del cliente.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 * @see VistaCliente
 * @see ConexionCliente
 * @see ConexionProperties
 */
public class ControlPrincipalC {

    private static final String HOST_SERVIDOR   = "localhost";
    private static final int    PUERTO_SERVIDOR = 7777;

    private final VistaCliente       vista;
    private final ConexionProperties cargadorPropiedades;

    /**
     * Construye el controlador, inicializa la vista y delega el registro
     * de listeners a ControlVista.
     */
    public ControlPrincipalC() {
        this.cargadorPropiedades = new ConexionProperties();
        this.vista               = new VistaCliente();
        new ControlVista(vista, this);
        this.vista.setVisible(true);
    }

    /**
     * Solicita la ruta del .properties a la Vista, carga los kimarites
     * con ConexionProperties y los muestra en la lista.
     * Invocado desde CargarKimarites.
     */
    public void cargarKimarites() {
        String ruta = vista.seleccionarRutaProperties();
        if (ruta == null) {
            vista.mostrarMensaje("No se seleccionó ningún archivo.");
            return;
        }

        List<String> nombres;
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
        vista.mostrarEstado("Kimarites cargados: " + nombres.size()
                + " técnicas disponibles");
    }

    /**
     * Valida los datos del formulario y conecta al servidor via ConexionCliente.
     * La conexión se ejecuta en un hilo de fondo para no bloquear el EDT de Swing.
     * Invocado desde Conectar.
     */
    public void conectarAlServidor() {
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
            vista.mostrarMensaje("El peso debe ser un número positivo válido.");
            return;
        }

        List<String> kimaritesSeleccionados = vista.getKimaritesSeleccionados();
        if (kimaritesSeleccionados.isEmpty()) {
            vista.mostrarMensaje("Debe seleccionar al menos un kimarite.");
            return;
        }

        // Formato del mensaje: "nombre|peso|k1,k2,k3..."
        String mensajeServidor = nombre + "|" + peso + "|"
                + String.join(",", kimaritesSeleccionados);

        vista.setBtnConectarHabilitado(false);
        vista.mostrarEstado("Conectando al servidor...");

        Thread hiloConexion = new Thread(
                () -> ejecutarCombate(mensajeServidor), "HiloConexionCliente");
        hiloConexion.setDaemon(true);
        hiloConexion.start();
    }

    /**
     * Ejecuta el flujo completo: conectar, enviar datos, esperar resultado y cerrar.
     * Se ejecuta en el hilo de fondo iniciado por conectarAlServidor().
     *
     * @param mensajeServidor datos del luchador formateados para el servidor
     */
    private void ejecutarCombate(String mensajeServidor) {
        ConexionCliente conexion = new ConexionCliente(HOST_SERVIDOR, PUERTO_SERVIDOR);
        try {
            conexion.conectar();
            actualizarVista("Conectado. Esperando al oponente...");

            conexion.enviar(mensajeServidor);

            String resultado = conexion.recibirRespuesta();

            boolean gano = "GANASTE!!!".equals(resultado);
            actualizarVista(gano ? "¡GANASTE EL COMBATE!" : "Perdiste el combate...");

            try {
                javax.swing.SwingUtilities.invokeAndWait(
                        () -> vista.mostrarResultadoCombate(gano));
            } catch (java.lang.reflect.InvocationTargetException ex) {
                // Si la vista lanzó excepción la ignoramos y continuamos
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            conexion.enviar("LISTO");

        } catch (IOException e) {
            actualizarVista("Error de conexión: " + e.getMessage());
        } finally {
            try { conexion.cerrar(); } catch (IOException ignored) {}
        }
    }

    /**
     * Actualiza el estado de la vista desde un hilo de fondo (respeta el EDT).
     *
     * @param mensaje texto a mostrar en la barra de estado
     */
    private void actualizarVista(String mensaje) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarEstado(mensaje));
    }
}