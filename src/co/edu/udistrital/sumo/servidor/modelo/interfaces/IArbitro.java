package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;

/**
 * Interfaz que representa al árbitro del combate de sumo.
 *
 * Define las acciones principales que controlan el desarrollo
 * de la pelea. Otras clases trabajan con esta interfaz
 * y no directamente con una implementación específica (DIP de SOLID).
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 */
public interface IArbitro {

    /**
     * Sube un luchador al dohyo en una posición específica.
     *
     * @param rikishi luchador que entra al combate
     * @param indice  posición en el dohyo (0 o 1)
     */
    void subirLuchador(Rikishi rikishi, int indice);

    /**
     * Bloquea el hilo hasta que los dos luchadores estén en el dohyo.
     *
     * @throws InterruptedException si ocurre una interrupción del hilo
     */
    void esperarAmbosLuchadores() throws InterruptedException;

    /**
     * Controla el turno de cada luchador.
     *
     * @param indiceLuchador índice del luchador que intenta jugar (0 o 1)
     * @throws InterruptedException si el hilo es interrumpido
     */
    void ejecutarTurno(int indiceLuchador) throws InterruptedException;

    /**
     * Indica si el combate ya terminó.
     *
     * @return true si ya hay un ganador
     */
    boolean isCombateTerminado();

    /**
     * Retorna el luchador ganador del combate.
     *
     * @return el Rikishi ganador, o null si aún no ha terminado
     */
    Rikishi getGanador();
}