package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.modelo.ConexionCliente;
import javax.swing.SwingWorker;

/**
 * Controlador de comunicacion via socket (MVC - Controlador de red).
 * 
 * Usa SwingWorker para ejecutar la comunicacion en segundo plano
 * sin bloquear la interfaz grafica (EDT - Event Dispatch Thread).
 * 
 * Protocolo de comunicacion bidireccional:
 *   1. Cliente envia:    "nombre|peso|k1,k2,..."  (datos del luchador)
 *   2. Cliente espera:   bloqueo en readLine() hasta que el servidor responda
 *   3. Servidor responde: "GANASTE", "PERDISTE" o "SIN_COMBATE"
 *   4. Cliente muestra resultado al usuario via JOptionPane
 *   5. Cliente confirma:  "LISTO" (servidor necesita saber que todos cerraron)
 *   6. Cliente cierra socket y termina
 * 
 * El SwingWorker garantiza que:
 *   - doInBackground() se ejecuta en un hilo separado (red, sin congelar UI)
 *   - invokeAndWait() muestra el JOptionPane en el EDT (thread-safe para Swing)
 * 
 * @author Grupo Programacion Avanzada
 */
public class ControlSocket {

    private final ControlPrincipalC cp;

    /** 
     * Constructor con inyeccion del controlador principal.
     * @param cp controlador que recibira el resultado del combate
     */
    public ControlSocket(ControlPrincipalC cp) {
        this.cp = cp;
    }

    /**
     * Envia los datos del luchador al servidor y espera resultado.
     * Todo se ejecuta en segundo plano (SwingWorker).
     * 
     * @param mensaje datos del luchador formateados como "nombre|peso|k1,k2,..."
     * @param ip      IP del servidor (de CredencialesClie.properties)
     * @param puerto  puerto del servidor (de CredencialesClie.properties)
     */
    public void enviarYEsperar(String mensaje, String ip, int puerto) {

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

            @Override
            protected String doInBackground() throws Exception {
                // Paso 1: Conectar al servidor via socket TCP
                ConexionCliente cnx = new ConexionCliente(ip, puerto);
                cnx.conectar();
                cp.actualizarEstado("Conectado. Esperando inicio del combate...");

                // Paso 2: Enviar datos del luchador al servidor
                cnx.enviar(mensaje);

                // Paso 3: Bloquear hasta recibir resultado del combate
                // El cliente NO visualiza el desarrollo del combate (requisito del enunciado)
                // Solo espera si gano o perdio
                String resultado = cnx.recibirRespuesta();

                // Paso 4: Mostrar resultado en el EDT (thread-safe)
                try {
                    javax.swing.SwingUtilities.invokeAndWait(
                        () -> cp.mostrarResultado(resultado));
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    // Error en la vista, continuamos para enviar LISTO
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

                // Paso 5: Confirmar al servidor que termino (protocolo LISTO)
                cnx.enviar("LISTO");
                cnx.cerrar();
                return resultado;
            }

            @Override
            protected void done() {
                // El resultado ya fue mostrado desde doInBackground
                // No se necesita accion adicional aqui
            }
        };

        worker.execute();  // Lanzar en segundo plano
    }
}
