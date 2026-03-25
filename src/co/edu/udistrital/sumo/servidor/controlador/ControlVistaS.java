package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.servidor.vista.VistaServidor;

import javax.swing.SwingUtilities;

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
        SwingUtilities.invokeLater(() -> vista.mostrarMensaje(msg));
    }

    /**
     * Actualiza el texto de estado en la barra inferior del log.
     * @param msg mensaje de estado
     */
    public void actualizarEstado(String msg) {
        SwingUtilities.invokeLater(() -> vista.actualizarEstado(msg));
    }

    /**
     * Cierra la ventana del servidor.
     */
    public void cerrar() {
        SwingUtilities.invokeLater(() -> vista.cerrar());
    }

    // ==================== Metodos de la grilla de conectados ====================

    /**
     * Registra visualmente un luchador conectado en la grilla.
     * Llamado por ControlPrincipalS cada vez que un cliente se conecta.
     *
     * @param nombre nombre del luchador
     * @param peso   peso en kg
     * @param indice slot 0-5 en la grilla
     */
    public void registrarLuchadorConectado(String nombre, double peso, int indice) {
        SwingUtilities.invokeLater(() ->
                vista.registrarLuchadorConectado(nombre, peso, indice));
    }

    /**
     * Muestra los dos combatientes seleccionados al azar.
     * Resalta sus slots en la grilla y actualiza el panel inferior.
     *
     * @param n1 nombre del luchador 1
     * @param p1 peso del luchador 1
     * @param n2 nombre del luchador 2
     * @param p2 peso del luchador 2
     */
    public void mostrarSeleccionCombatientes(String n1, double p1, String n2, double p2) {
        SwingUtilities.invokeLater(() ->
                vista.mostrarSeleccionCombatientes(n1, p1, n2, p2));
    }

    /**
     * Muestra un luchador en el panel de combate (Dohyo).
     *
     * @param nombre nombre del luchador
     * @param peso   peso en kg
     * @param indice 0 = izquierda, 1 = derecha
     */
    public void mostrarLuchadorEnDohyo(String nombre, double peso, int indice) {
        SwingUtilities.invokeLater(() ->
                vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    /**
     * Muestra el inicio del combate y trae la ventana al frente.
     *
     * @param n1 nombre del luchador 1
     * @param n2 nombre del luchador 2
     */
    public void mostrarInicioCombate(String n1, String n2) {
        SwingUtilities.invokeLater(() ->
                vista.mostrarInicioCombate(n1, n2));
    }

    /**
     * Muestra el ganador en el panel dorado y actualiza la grilla.
     *
     * @param nombre    nombre del ganador
     * @param victorias total de victorias
     */
    public void mostrarGanador(String nombre, int victorias) {
        SwingUtilities.invokeLater(() ->
                vista.mostrarGanador(nombre, victorias));
    }

    /**
     * Marca un luchador como "ya combatio" en la grilla.
     *
     * @param nombre nombre del luchador que ya participo
     */
    public void marcarLuchadorParticipo(String nombre) {
        SwingUtilities.invokeLater(() ->
                vista.marcarLuchadorParticipo(nombre));
    }

    /**
     * Resetea la seleccion visual entre combates.
     * Limpia los paneles de combate y devuelve los slots disponibles a estado normal.
     */
    public void resetearSeleccion() {
        SwingUtilities.invokeLater(() ->
                vista.resetearSeleccion());
    }

    // ==================== Implementacion ICombateObservador ====================

    @Override
    public void onLuchadorLlego(String nombre, double peso, int indice) {
        mostrar("Luchador " + (indice + 1) + " llego: " + nombre
                + " (" + String.format("%.1f", peso) + " kg)");
        SwingUtilities.invokeLater(() ->
                vista.mostrarLuchadorEnDohyo(nombre, peso, indice));
    }

    @Override
    public void onCombateIniciado(String nombre1, String nombre2) {
        mostrar("COMBATE: " + nombre1 + " VS " + nombre2);
        SwingUtilities.invokeLater(() ->
                vista.mostrarInicioCombate(nombre1, nombre2));
    }

    @Override
    public void onKimariteEjecutado(String luchador, String kimarite, boolean expulsado) {
        String resultado = expulsado ? "EXPULSADO" : "resiste";
        mostrar(luchador + " [" + kimarite + "] -> " + resultado);
        SwingUtilities.invokeLater(() ->
                vista.mostrarKimarite(luchador, kimarite, expulsado));
    }

    @Override
    public void onCombateTerminado(String ganador, int victorias) {
        mostrar("GANADOR: " + ganador + " | Victorias: " + victorias);
        SwingUtilities.invokeLater(() ->
                vista.mostrarGanador(ganador, victorias));
    }
}