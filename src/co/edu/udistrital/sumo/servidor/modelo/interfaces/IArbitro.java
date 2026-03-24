package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.modelo.cliente.Rikishi;

/**
 * Interfaz que representa al árbitro del combate de sumo.
 *
 * Aquí se definen las acciones principales que controlan
 * cómo se desarrolla la pelea entre los luchadores.
 *
 * La idea es que otras clases trabajen con esta interfaz
 * y no directamente con una implementación específica.
 */
public interface IArbitro {

    /**
     * Sube un luchador al dohyo en una posición específica.
     *
     * @param rikishi luchador que entra al combate
     * @param indice posición en el dohyo (0 o 1)
     */
    void subirLuchador(Rikishi rikishi, int indice);

    /**
     * Hace que el hilo espere hasta que los dos luchadores
     * ya estén listos en el dohyo.
     *
     * @throws InterruptedException si ocurre una interrupción del hilo
     */
    void esperarAmbosLuchadores() throws InterruptedException;

    /**
     * Controla el turno de cada luchador.
     * Si no es su turno, el hilo espera un tiempo antes de continuar.
     *
     * @param indiceLuchador indica cuál luchador está intentando jugar (0 o 1)
     * @throws InterruptedException si el hilo es interrumpido
     */
    void ejecutarTurno(int indiceLuchador) throws InterruptedException;

    /**
     * Indica si el combate ya terminó.
     *
     * @return true si ya hay un ganador, false si sigue en curso
     */
    boolean isCombateTerminado();

    /**
     * Devuelve el luchador ganador del combate.
     *
     * @return el Rikishi ganador, o null si aún no ha terminado
     */
    Rikishi getGanador();
}