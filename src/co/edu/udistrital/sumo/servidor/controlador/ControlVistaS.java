package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.servidor.vista.VistaServidor;

/**
 * Gestiona la VistaServidor e implementa ICombateObservador.
 * Recibe notificaciones del Dohyo y las muestra en la vista.
 * Usa SwingUtilities.invokeLater para no modificar la UI desde hilos de red.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlVistaS implements ICombateObservador {

    private final VistaServidor vista;

    /**
     * Constructor que crea y muestra la VistaServidor.
     */
    public ControlVistaS() {
        vista = new VistaServidor();
        vista.setVisible(true);
    }

    /**
     * Muestra un mensaje en el log de la vista.
     * @param msg mensaje a mostrar
     */
    public void mostrar(String msg) {
        javax.swing.SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
    }

    /**
     * Cierra la ventana del servidor.
     */
    public void cerrar() {
        javax.swing.SwingUtilities.invokeLater(() -> vista.cerrar());
    }

    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        mostrar("Luchador " + (indice + 1) + " llego: " + nombre
                + " (" + String.format("%.1f", peso) + " kg)");
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    @Override
    public void onCombateIniciado(String nombre1, String nombre2) {
        mostrar("COMBATE: " + nombre1 + " VS " + nombre2);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarInicioCombate(nombre1, nombre2));
    }

    @Override
    public void onKimariteEjecutado(String luchador, String kimarite, boolean expulsado) {
        String resultado = expulsado ? "EXPULSADO" : "resiste";
        mostrar(luchador + " [" + kimarite + "] -> " + resultado);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarKimarite(luchador, kimarite, expulsado));
    }

    @Override
    public void onCombateTerminado(String ganador, int victorias) {
        mostrar("GANADOR: " + ganador + " | Victorias: " + victorias);
        javax.swing.SwingUtilities.invokeLater(
            () -> vista.mostrarGanador(ganador, victorias));
    }
}
