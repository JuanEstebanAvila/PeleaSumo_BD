package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionProperties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controlador principal del cliente.
 * Coordina ControlVista y ControlSocket.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalC {

    private final ControlVista       controlVista;
    private final ControlSocket      controlSocket;
    private final ConexionProperties cnxProperties;

    public ControlPrincipalC() {
        cnxProperties = new ConexionProperties();

        String rutaCredenciales = seleccionarCredenciales();
        if (rutaCredenciales == null) {
            System.exit(0);
            controlVista  = null;
            controlSocket = null;
            return;
        }

        try {
            cnxProperties.cargarConfiguracion(rutaCredenciales);
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar credenciales: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            controlVista  = null;
            controlSocket = null;
            return;
        }

        controlVista  = new ControlVista(this);
        controlSocket = new ControlSocket(this);
    }

    private String seleccionarCredenciales() {
        JFileChooser fc = new JFileChooser(new File("Data/Cliente/"));
        fc.setFileFilter(new FileNameExtensionFilter(
                "Archivo de credenciales (*.properties)", "properties"));
        fc.setDialogTitle("Seleccione las credenciales del cliente");
        int resultado = fc.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public void cargarKimarites(String ruta) {
        try {
            List<String> kimarites = cnxProperties.cargarKimarites(ruta);
            if (kimarites.isEmpty()) {
                controlVista.mostrarMensaje(
                        "El archivo no contiene kimarites validos.");
                return;
            }
            controlVista.mostrarKimarites(kimarites);
            controlVista.mostrarEstado(
                    "Listo: " + kimarites.size() + " tecnicas cargadas.");
        } catch (IOException e) {
            controlVista.mostrarMensaje(
                    "Error al leer el archivo: " + e.getMessage());
        }
    }

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

        String mensaje = nombre.trim() + "|" + peso + "|"
                + String.join(",", kimarites);

        controlVista.habilitarConectar(false);
        controlVista.mostrarEstado("Conectando al servidor...");
        controlSocket.enviarYEsperar(mensaje,
                cnxProperties.getIp(), cnxProperties.getPuerto());
    }

    /**
     * Muestra el resultado del combate.
     * @param resultado "GANASTE", "PERDISTE" o "SIN_COMBATE"
     */
    public void mostrarResultado(String resultado) {
        controlVista.mostrarResultado(resultado);
    }

    public void actualizarEstado(String msg) {
        javax.swing.SwingUtilities.invokeLater(
                () -> controlVista.mostrarEstado(msg));
    }
}
