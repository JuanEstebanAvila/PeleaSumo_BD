package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionCliente;
import java.io.IOException;
import javax.swing.SwingWorker;

/**
 * Controla la comunicacion del cliente con el servidor via socket.
 * Usa SwingWorker para no bloquear la interfaz grafica (EDT).
 *
 * Protocolo:
 *   Cliente envia:  "nombre|peso|k1,k2,..."
 *   Servidor responde: "GANASTE", "PERDISTE" o "SIN_COMBATE"
 *   Cliente confirma: "LISTO"
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlSocket {

    private final ControlPrincipalC cp;

    /**
     * Constructor con inyeccion del controlador principal.
     * @param cp controlador principal del cliente
     */
    public ControlSocket(ControlPrincipalC cp) {
        this.cp = cp;
    }

    /**
     * Envia los datos al servidor y espera el resultado en segundo plano.
     * Al recibir el resultado muestra el dialogo al usuario y envia LISTO.
     *
     * @param mensaje datos formateados del luchador
     * @param ip      IP del servidor
     * @param puerto  puerto del servidor
     */
    public void enviarYEsperar(String mensaje, String ip, int puerto) {

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                ConexionCliente cnx = new ConexionCliente(ip, puerto);
                cnx.conectar();
                cp.actualizarEstado("Conectado. Esperando inicio del combate...");

                // Enviar datos al servidor
                cnx.enviar(mensaje);

                // Bloquear hasta recibir el resultado
                String resultado = cnx.recibirRespuesta();

                // Mostrar resultado en el EDT y esperar que el usuario presione OK
                boolean gano = "GANASTE".equals(resultado);
                try {
                    javax.swing.SwingUtilities.invokeAndWait(() -> cp.mostrarResultado(gano));
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    // La vista pudo haber lanzado un error, continuamos
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // El usuario ya presiono OK: confirmar al servidor
                cnx.enviar("LISTO");
                cnx.cerrar();
                return resultado;
            }

            @Override
            protected void done() {
                // El resultado ya fue mostrado desde doInBackground
            }
        };

        worker.execute();
    }
}
