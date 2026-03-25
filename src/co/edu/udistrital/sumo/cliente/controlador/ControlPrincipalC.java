package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionCliente;
import co.edu.udistrital.sumo.cliente.modelo.ConexionProperties;
import java.io.IOException;
import java.util.List;

/**
 * Controlador principal del cliente.
 * Coordina ControlVista y ControlSocket.
 * Carga la configuracion desde el properties al inicio.
 *
 * PROHIBIDO: JFileChooser, ServerSocket, componentes Swing directos.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalC {

    private final ControlVista       controlVista;
    private final ControlSocket      controlSocket;
    private final ConexionProperties cnxProperties;

    /**
     * Crea los controles y muestra la vista.
     * El JFileChooser para el properties se abre desde la vista.
     */
    public ControlPrincipalC() {
        cnxProperties = new ConexionProperties();
        controlVista  = new ControlVista(this);
        controlSocket = new ControlSocket(this);
    }

    /**
     * Carga los kimarites y la configuracion del socket
     * desde el archivo properties indicado.
     * @param ruta ruta del archivo properties
     */
    public void cargarKimarites(String ruta) {
        try {
            cnxProperties.cargarConfiguracion(ruta);
            List<String> kimarites = cnxProperties.cargarKimarites(ruta);
            if (kimarites.isEmpty()) {
                controlVista.mostrarMensaje("El archivo no contiene kimarites validos.");
                return;
            }
            controlVista.mostrarKimarites(kimarites);
            controlVista.mostrarEstado("Listo: " + kimarites.size() + " tecnicas cargadas.");
        } catch (IOException e) {
            controlVista.mostrarMensaje("Error al leer el archivo: " + e.getMessage());
        }
    }

    /**
     * Valida los datos del formulario y delega la conexion al ControlSocket.
     * @param nombre   nombre del luchador
     * @param peso     peso del luchador
     * @param kimarites tecnicas seleccionadas
     */
    public void conectar(String nombre, double peso, List<String> kimarites) {
        if (nombre == null || nombre.trim().isEmpty()) {
            controlVista.mostrarMensaje("Ingrese el nombre del luchador.");
            return;
        }
        if (peso <= 0) {
            controlVista.mostrarMensaje("El peso debe ser mayor a cero.");
            return;
        }
        if (kimarites.isEmpty()) {
            controlVista.mostrarMensaje("Seleccione al menos un kimarite.");
            return;
        }

        // Formatear mensaje para el servidor: nombre|peso|k1,k2,...
        String mensaje = nombre.trim() + "|" + peso + "|" + String.join(",", kimarites);

        controlVista.habilitarConectar(false);
        controlVista.mostrarEstado("Conectando al servidor...");
        controlSocket.enviarYEsperar(mensaje,
                cnxProperties.getIp(), cnxProperties.getPuerto());
    }

    /**
     * Muestra el resultado del combate en la vista.
     * Llamado por ControlSocket cuando llega la respuesta del servidor.
     * @param gano true si el luchador gano
     */
    public void mostrarResultado(boolean gano) {
        controlVista.mostrarResultado(gano);
    }

    /**
     * Actualiza el texto de estado en la barra inferior de la vista.
     * @param msg mensaje a mostrar
     */
    public void actualizarEstado(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> controlVista.mostrarEstado(msg));
    }
}
