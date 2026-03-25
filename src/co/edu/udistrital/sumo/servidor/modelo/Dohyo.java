package co.edu.udistrital.sumo.servidor.modelo;

/**
 * POJO que representa el estado puro del ring de sumo (dohyo).
 * No contiene lógica de combate — solo almacena el estado.
 * La lógica reside en ControladorDohyo.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 */
public class Dohyo {

    /** Los dos luchadores en el ring */
    private Rikishi[] luchadores = new Rikishi[2];

    /** Turno actual: 0 = luchador 0, 1 = luchador 1 */
    private int turnoActual = 0;

    /** true cuando ya se anunció el inicio del combate */
    private boolean combateAnunciado = false;

    /** true cuando el combate terminó */
    private boolean combateTerminado = false;

    /** Luchador ganador, null mientras el combate no termina */
    private Rikishi ganador = null;

    /**
     * Asigna un luchador en la posición indicada.
     *
     * @param rikishi luchador a asignar
     * @param indice  posición (0 o 1)
     */
    public void setLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
    }

    /**
     * Retorna el luchador en la posición indicada.
     *
     * @param indice posición (0 o 1)
     * @return Rikishi en esa posición, o null si aún no llegó
     */
    public Rikishi getLuchador(int indice) {
        return luchadores[indice];
    }

    /**
     * Indica si ambos luchadores ya están en el dohyo.
     *
     * @return true si los dos índices tienen un Rikishi asignado
     */
    public boolean ambosLuchadoresPresentes() {
        return luchadores[0] != null && luchadores[1] != null;
    }

    // Getters y setters

    public int getTurnoActual()              { return turnoActual; }
    public void setTurnoActual(int t)        { this.turnoActual = t; }

    public boolean isCombateAnunciado()      { return combateAnunciado; }
    public void setCombateAnunciado(boolean b) { this.combateAnunciado = b; }

    public boolean isCombateTerminado()      { return combateTerminado; }
    public void setCombateTerminado(boolean b) { this.combateTerminado = b; }

    public Rikishi getGanador()              { return ganador; }
    public void setGanador(Rikishi g)        { this.ganador = g; }
}