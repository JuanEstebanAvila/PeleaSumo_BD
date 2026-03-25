package co.edu.udistrital.sumo.servidor.modelo.interfaces;

/**
 * Contrato del observador de eventos del combate.
 * Implementado por ControlVistaS para recibir notificaciones
 * del Dohyo sin que este conozca la vista (patron Observer).
 *
 * @author Grupo Programacion Avanzada
 */
public interface ICombateObservador {

    /**
     * Un luchador llego al dohyo.
     * @param nombre nombre del luchador
     * @param peso   peso del luchador
     * @param indice posicion en el dohyo (0 o 1)
     */
    void onLuchadorLlego(String nombre, double peso, int indice);

    /**
     * Los dos luchadores estan listos y el combate inicia.
     * @param nombre1 nombre del primer luchador
     * @param nombre2 nombre del segundo luchador
     */
    void onCombateIniciado(String nombre1, String nombre2);

    /**
     * Un luchador ejecuto una tecnica.
     * @param luchador  nombre del atacante
     * @param kimarite  nombre de la tecnica
     * @param expulsado true si el oponente salio del dohyo
     */
    void onKimariteEjecutado(String luchador, String kimarite, boolean expulsado);

    /**
     * El combate termino con un ganador.
     * @param ganador   nombre del ganador
     * @param victorias total de victorias del ganador
     */
    void onCombateTerminado(String ganador, int victorias);
}
