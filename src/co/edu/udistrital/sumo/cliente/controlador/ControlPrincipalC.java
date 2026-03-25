package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionProperties;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Controlador principal del cliente (MVC - Controlador).
 * 
 * Coordina la interaccion entre:
 *   - ControlVista: gestiona la interfaz grafica del cliente
 *   - ControlSocket: gestiona la comunicacion con el servidor
 *   - ConexionProperties: carga configuracion desde archivos .properties
 * 
 * Flujo completo del cliente:
 *   1. Constructor: abre JFileChooser para seleccionar CredencialesClie.properties
 *      -> carga IP_SERVIDOR y PUERTO desde el archivo
 *   2. Se muestra la VistaCliente con el formulario
 *   3. Usuario presiona "Cargar Kimarites" -> cargarKimarites() lee otro .properties
 *   4. Usuario llena nombre, peso, selecciona tecnicas y presiona "Conectar"
 *   5. conectar() valida datos y delega al ControlSocket
 *   6. ControlSocket envia datos al servidor via socket y espera respuesta
 *   7. Servidor responde GANASTE/PERDISTE/SIN_COMBATE
 *   8. Se muestra JOptionPane con el resultado y se cierra la ventana
 * 
 * Principios SOLID aplicados:
 *   - SRP: solo coordina, no tiene logica de UI ni de red
 *   - OCP: IP y puerto vienen de properties, no estan quemados
 *   - DIP: depende de abstracciones (interfaces de ControlVista)
 * 
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalC {

    // Referencias a los otros controladores (composicion, no herencia)
    private final ControlVista       controlVista;
    private final ControlSocket      controlSocket;
    // Modelo: carga configuracion desde archivos .properties
    private final ConexionProperties cnxProperties;

    /**
     * Constructor del controlador principal.
     * 
     * Paso 1: Crea el modelo ConexionProperties.
     * Paso 2: Abre JFileChooser para seleccionar el archivo de credenciales.
     *         Si el usuario cancela, termina la aplicacion.
     * Paso 3: Carga IP_SERVIDOR y PUERTO desde el archivo seleccionado.
     * Paso 4: Crea ControlVista (que a su vez crea y muestra la VistaCliente).
     * Paso 5: Crea ControlSocket para la futura comunicacion con el servidor.
     */
    public ControlPrincipalC() {
        cnxProperties = new ConexionProperties();

        // Seleccionar archivo de credenciales (IP y puerto del servidor)
        String rutaCredenciales = seleccionarCredenciales();
        if (rutaCredenciales == null) {
            System.exit(0);  // Usuario cancelo -> terminar
            controlVista  = null;
            controlSocket = null;
            return;
        }

        // Cargar IP y PUERTO del properties seleccionado
        try {
            cnxProperties.cargarConfiguracion(rutaCredenciales);
        } catch (IOException e) {
            // Mostrar error y terminar (JOptionPane permitido para mensajes informativos)
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar credenciales: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            controlVista  = null;
            controlSocket = null;
            return;
        }

        // Crear los controladores de vista y socket
        controlVista  = new ControlVista(this);   // Crea y muestra la ventana
        controlSocket = new ControlSocket(this);   // Listo para enviar datos
    }

    /**
     * Abre un JFileChooser para que el usuario seleccione
     * el archivo CredencialesClie.properties.
     * 
     * @return ruta absoluta del archivo, o null si cancelo
     */
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

    /**
     * Carga los kimarites desde el archivo kimarites.properties.
     * Llamado cuando el usuario presiona el boton "Cargar Kimarites".
     * 
     * NOTA: este metodo NO carga credenciales. Eso ya se hizo en el constructor.
     * Aqui solo se leen las claves que contienen "kimarite" en su nombre.
     * 
     * @param ruta ruta del archivo .properties con las tecnicas
     */
    public void cargarKimarites(String ruta) {
        try {
            List<String> kimarites = cnxProperties.cargarKimarites(ruta);
            if (kimarites.isEmpty()) {
                controlVista.mostrarMensaje(
                        "El archivo no contiene kimarites validos.");
                return;
            }
            // Poblar la JList de la vista con las tecnicas cargadas
            controlVista.mostrarKimarites(kimarites);
            controlVista.mostrarEstado(
                    "Listo: " + kimarites.size() + " tecnicas cargadas.");
        } catch (IOException e) {
            controlVista.mostrarMensaje(
                    "Error al leer el archivo: " + e.getMessage());
        }
    }

    /**
     * Valida los datos del formulario y delega la conexion al ControlSocket.
     * Llamado cuando el usuario presiona "ENTRAR AL DOHYO".
     * 
     * El mensaje que se envia al servidor tiene formato: "nombre|peso|k1,k2,k3"
     * donde k1,k2,k3 son los kimarites seleccionados separados por coma.
     * 
     * @param nombre    nombre del luchador ingresado en el formulario
     * @param peso      peso en kg ingresado en el formulario
     * @param kimarites lista de tecnicas seleccionadas de la JList
     */
    public void conectar(String nombre, double peso, List<String> kimarites) {
        // Validaciones de datos
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

        // Construir mensaje con formato del protocolo: nombre|peso|k1,k2,...
        String mensaje = nombre.trim() + "|" + peso + "|"
                + String.join(",", kimarites);

        // Deshabilitar boton para evitar doble envio
        controlVista.habilitarConectar(false);
        controlVista.mostrarEstado("Conectando al servidor...");

        // Delegar al ControlSocket (ejecuta en segundo plano con SwingWorker)
        controlSocket.enviarYEsperar(mensaje,
                cnxProperties.getIp(), cnxProperties.getPuerto());
    }

    /**
     * Muestra el resultado del combate en la vista.
     * Llamado por ControlSocket cuando llega la respuesta del servidor.
     * 
     * @param resultado "GANASTE", "PERDISTE" o "SIN_COMBATE"
     */
    public void mostrarResultado(String resultado) {
        controlVista.mostrarResultado(resultado);
    }

    /**
     * Actualiza el texto de estado en la barra inferior de la vista.
     * Usa invokeLater porque puede ser llamado desde un hilo no-EDT.
     * 
     * @param msg mensaje de estado
     */
    public void actualizarEstado(String msg) {
        javax.swing.SwingUtilities.invokeLater(
                () -> controlVista.mostrarEstado(msg));
    }
}
