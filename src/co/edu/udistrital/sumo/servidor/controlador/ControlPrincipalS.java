//Clase "control principal" en el anterior proyecto del servidor (COMBINA MUCHAS RESPONSABILIDADES, NO TIENE DESACOPLAMINETO)
package co.edu.udistrital.sumo.controlador.servidor;

import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.servidor.ConexionServidor;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import co.edu.udistrital.sumo.vista.servidor.VistaServidor;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Controlador del servidor del Combate de Sumo.
 *
 * Gestiona el ciclo de vida: abre el servidor, acepta dos conexiones,
 * lanza un HiloLuchador por cada una, y cierra cuando ambos clientes
 * confirman con "LISTO".
 *
 * El cierre lo controla un CountDownLatch(2):
 *   - Cada HiloLuchador llama countDown() en su finally (despues de LISTO)
 *   - Cuando llega a 0, se llama vista.cerrar()
 *   - La vista NUNCA llama System.exit() por su cuenta
 *
 * PROHIBIDO: ServerSocket directo, logica de combate.
 *
 * @author Grupo Programación avanzada
 * @version 3.0
 */
public class ControladorServidor implements ICombateObservador {

    private static final int PUERTO = 9999;

    private final VistaServidor    vista;
    private final ConexionServidor conexionServidor;
    private final Dohyo            dohyo;
    private final ControladorDohyo controladorDohyo;

    // Cuenta hacia 0 cuando ambos clientes enviaron LISTO
    private final CountDownLatch latchCierre = new CountDownLatch(2);

    public ControladorServidor() {
        this.dohyo            = new Dohyo();
        this.controladorDohyo = new ControladorDohyo(dohyo);
        this.conexionServidor = new ConexionServidor(PUERTO);
        this.vista            = new VistaServidor();
        this.controladorDohyo.agregarObservador(this);
        this.vista.setVisible(true);
    }

    public static void iniciar() {
        ControladorServidor servidor = new ControladorServidor();
        Thread hilo = new Thread(servidor::iniciarServidor, "HiloServidorSocket");
        hilo.setDaemon(false);
        hilo.start();
    }

    public void iniciarServidor() {
        actualizarVista("Servidor iniciado en el puerto " + PUERTO);
        actualizarVista("Esperando a los dos luchadores...");

        try {
            conexionServidor.iniciar();

            for (int i = 0; i < conexionServidor.getMaxLuchadores(); i++) {
                Socket socketCliente = conexionServidor.aceptarConexion();
                actualizarVista("Luchador " + (i + 1) + " conectado desde " + socketCliente.getInetAddress().getHostAddress());

                // HiloLuchador recibe IArbitro (abstraccion), no ControladorDohyo (concrecion)
                HiloLuchador hilo = new HiloLuchador(socketCliente, controladorDohyo, i, latchCierre);
                hilo.start();
            }

            conexionServidor.cerrar();

            // Bloquea este hilo (no el EDT) hasta que ambos clientes envien LISTO
            latchCierre.await();

            // Ambos confirmaron: cerrar en el EDT
            javax.swing.SwingUtilities.invokeLater(() -> vista.cerrar());

        } catch (IOException e) {
            actualizarVista("Error en el servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Notifica la llegada de un luchador al log y actualiza su panel en la vista
@Override
public void onLuchadorLlego(String nombre, double peso, int indice) {
    actualizarVista("Luchador " + (indice + 1) + " llego: " + nombre + " (" + String.format("%.1f", peso) + " kg)");
    javax.swing.SwingUtilities.invokeLater(
        () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
}

// Registra el inicio del combate en el log y activa la vista del duelo
@Override
public void onCombateIniciado(String n1, String n2) {
    actualizarVista("COMBATE INICIADO: " + n1 + " V.S " + n2);
    javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarInicioCombate(n1, n2));
}

// Registra el kimarite ejecutado y muestra su imagen en la vista
@Override
public void onKimariteEjecutado(String luchador, String kimarite, boolean expulsado) {
    String res = expulsado ? "EXPULSADO" : "resiste";
    actualizarVista(luchador + " [" + kimarite + "] -> " + res);
    javax.swing.SwingUtilities.invokeLater(
        () -> vista.mostrarKimarite(luchador, kimarite, expulsado));
}

// Registra al ganador en el log y muestra el panel de victoria
@Override
public void onCombateTerminado(String ganador, int victorias) {
    actualizarVista("GANADOR: " + ganador + " | Victorias: " + victorias);
    javax.swing.SwingUtilities.invokeLater(
        () -> vista.mostrarGanador(ganador, victorias));
}

// Envía un mensaje al log de la vista desde cualquier hilo usando invokeLater
private void actualizarVista(String msg) {
    javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
}
}
