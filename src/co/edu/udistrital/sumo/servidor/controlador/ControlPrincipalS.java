package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Dohyo;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionServidor;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.servidor.vista.VistaServidor;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Controlador principal del servidor del Combate de Sumo.
 *
 * Gestiona el ciclo de vida: abre el servidor, acepta dos conexiones,
 * lanza un HiloLuchador por cada una, y cierra cuando ambos clientes
 * confirman con "LISTO".
 *
 * El cierre lo controla un CountDownLatch(2):
 *   - Cada HiloLuchador llama countDown() en su finally
 *   - Cuando llega a 0, se cierra la vista
 *
 * PROHIBIDO: ServerSocket directo, lógica de combate.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 */
public class ControlPrincipalS implements ICombateObservador {

    private static final int PUERTO = 7777;

    private final VistaServidor     vista;
    private final ConexionServidor  conexionServidor;
    private final Dohyo             dohyo;
    private final ControladorDohyo  controladorDohyo;

    private final CountDownLatch latchCierre = new CountDownLatch(2);

    /**
     * Construye el controlador del servidor, inicializa todos los componentes
     * y registra este controlador como observador del combate.
     */
    public ControlPrincipalS() {
        this.dohyo           = new Dohyo();
        this.controladorDohyo = new ControladorDohyo(dohyo);
        this.conexionServidor = new ConexionServidor(PUERTO);
        this.vista           = new VistaServidor();
        this.controladorDohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }

    /**
     * Método estático de entrada invocado desde LauncherServidor.
     * Crea la instancia y arranca el hilo del servidor.
     */
    public static void iniciar() {
        ControlPrincipalS servidor = new ControlPrincipalS();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }

    /**
     * Abre el ServerSocket, acepta las dos conexiones y lanza
     * un HiloLuchador por cada una. Bloquea hasta que ambos
     * clientes envíen "LISTO".
     */
    public void iniciarServidor() {
        actualizarVista("Servidor iniciado en el puerto " + PUERTO);
        actualizarVista("Esperando a los dos luchadores...");

        try {
            conexionServidor.iniciar();

            for (int i = 0; i < 2; i++) {
                Socket socketCliente = conexionServidor.aceptarConexion();
                actualizarVista("Luchador " + (i + 1) + " conectado desde "
                        + socketCliente.getInetAddress().getHostAddress());

                HiloLuchador hilo = new HiloLuchador(
                        socketCliente, controladorDohyo, i, latchCierre);
                hilo.start();
            }

            conexionServidor.cerrar();
            latchCierre.await();

            javax.swing.SwingUtilities.invokeLater(() -> vista.cerrar());

        } catch (IOException e) {
            actualizarVista("Error en el servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        actualizarVista("Luchador " + (indice + 1) + " llegó: " + nombre
                + " (" + String.format("%.1f", peso) + " kg)");
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    @Override
    public void onCombateIniciado(String n1, String n2) {
        actualizarVista("COMBATE INICIADO: " + n1 + " VS " + n2);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarInicioCombate(n1, n2));
    }

    @Override
    public void onKimariteEjecutado(String luchador, String kimarite,
                                     boolean expulsado) {
        String res = expulsado ? "EXPULSADO" : "resiste";
        actualizarVista(luchador + " [" + kimarite + "] -> " + res);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarKimarite(luchador, kimarite, expulsado));
    }

    @Override
    public void onCombateTerminado(String ganador, int victorias) {
        actualizarVista("GANADOR: " + ganador + " | Victorias: " + victorias);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarGanador(ganador, victorias));
    }

    /**
     * Envía un mensaje al log de la vista desde cualquier hilo.
     *
     * @param msg mensaje a mostrar
     */
    private void actualizarVista(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
    }
}