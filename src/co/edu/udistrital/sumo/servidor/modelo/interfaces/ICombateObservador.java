package co.edu.udistrital.sumo.servidor.modelo.interfaces;

/**
 * Interfaz que maneja los eventos que ocurren durante el combate.
 *
 * Se usa para notificar cuando pasa algo importante,
 * como la llegada de luchadores o el final del combate.
 *
 * Esto permite que el controlador no dependa directamente de la vista.
 */
public interface ICombateObservador {

    /**
     * Se ejecuta cuando un luchador llega al dohyo.
     *
     * @param nombre nombre del luchador
     * @param peso peso del luchador
     * @param indice posición en el dohyo (0 = primero, 1 = segundo)
     */
    void onLuchadorLlego(String nombre, double peso, int indice);

    /**
     * Se ejecuta cuando ya están los dos luchadores
     * y el combate comienza.
     *
     * @param nombreLuchador1 nombre del primer luchador
     * @param nombreLuchador2 nombre del segundo luchador
     */
    void onCombateIniciado(String nombreLuchador1, String nombreLuchador2);

    /**
     * Se ejecuta cuando un luchador realiza una técnica (kimarite).
     *
     * @param nombreLuchador nombre del luchador que atacó
     * @param nombreKimarite nombre de la técnica usada
     * @param expulsado indica si el oponente salió del dohyo
     */
    void onKimariteEjecutado(String nombreLuchador,
                              String nombreKimarite,
                              boolean expulsado);

    /**
     * Se ejecuta cuando el combate termina y ya hay un ganador.
     *
     * @param nombreGanador nombre del luchador ganador
     * @param victoriasGanador número de victorias acumuladas
     */
    void onCombateTerminado(String nombreGanador, int victoriasGanador);
}