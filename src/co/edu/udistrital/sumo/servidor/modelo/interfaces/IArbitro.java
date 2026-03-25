package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;

/**
 * Contrato del arbitro del combate.
 * Lo implementa Dohyo. HiloLuchador depende de esta interfaz,
 * no de Dohyo directamente (principio DIP de SOLID).
 *
 * @author Grupo Programacion Avanzada
 */
public interface IArbitro {

    /**
     * Sube un luchador al dohyo en el indice indicado (0 o 1).
     * @param rikishi luchador que entra
     * @param indice  posicion en el dohyo
     */
    void subirLuchador(Rikishi rikishi, int indice);

    /**
     * Bloquea el hilo hasta que los dos luchadores esten listos.
     * @throws InterruptedException si el hilo es interrumpido
     */
    void esperarAmbosLuchadores() throws InterruptedException;

    /**
     * Ejecuta el turno del luchador indicado.
     * Espera hasta 500ms si no es su turno.
     * @param indiceLuchador 0 o 1
     * @throws InterruptedException si el hilo es interrumpido
     */
    void ejecutarTurno(int indiceLuchador) throws InterruptedException;

    /**
     * Indica si el combate ya termino.
     * @return true si hay ganador
     */
    boolean isCombateTerminado();

    /**
     * Retorna el luchador ganador.
     * @return ganador o null si aun no termina
     */
    Rikishi getGanador();
}
