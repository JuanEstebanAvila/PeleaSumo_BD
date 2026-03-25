package co.edu.udistrital.sumo.servidor.modelo;

/**
 * Representa un luchador de sumo - Rikishi (MVC - Modelo).
 * 
 * Es un POJO (Plain Old Java Object) que solo almacena datos.
 * No tiene logica de combate, sincronizacion, ni acceso a BD.
 * 
 * Se crea en dos contextos:
 *   1. HiloLuchador.parsearRikishi(): al recibir datos del cliente via socket
 *   2. RikishiDAO.rsARikishi(): al leer un registro de la BD
 * 
 * Atributos principales:
 *   - nombre:      nombre del luchador (identificador unico)
 *   - peso:        peso en kg
 *   - victorias:   combates ganados (se incrementa en el Dohyo)
 *   - kimarites:   arreglo de tecnicas en las que es experto
 *   - dentroDohyo: true si esta actualmente en el ring
 *   - rival:       nombre del oponente asignado en el combate actual
 *   - participo:   true si ya combatio en el torneo (control en memoria)
 *
 * @author Grupo Programacion Avanzada
 */
public class Rikishi {

    private String   nombre;
    private double   peso;
    private int      victorias;
    private String[] kimarites;
    private boolean  dentroDohyo;
    private String   rival;         // nombre del rival asignado en el combate
    private boolean  participo;     // true si ya combatio en el torneo

    /**
     * Constructor completo, usado al leer desde la base de datos.
     * @param nombre    nombre del luchador
     * @param peso      peso en kg
     * @param victorias victorias acumuladas
     * @param kimarites tecnicas del luchador
     */
    public Rikishi(String nombre, double peso, int victorias, String[] kimarites) {
        this.nombre      = nombre;
        this.peso        = peso;
        this.victorias   = victorias;
        this.kimarites   = kimarites;
        this.dentroDohyo = false;
        this.participo   = false;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public int getVictorias() {
        return victorias;
    }

    public void setVictorias(int victorias) {
        this.victorias = victorias;
    }

    public String[] getKimarites() {
        return kimarites;
    }

    public void setKimarites(String[] kimarites) {
        this.kimarites = kimarites;
    }

    public boolean isDentroDohyo() {
        return dentroDohyo;
    }

    public void setDentroDohyo(boolean dentroDohyo) {
        this.dentroDohyo = dentroDohyo;
    }

    public String getRival() {
        return rival;
    }

    public void setRival(String rival) {
        this.rival = rival;
    }

    public boolean isParticipo() {
        return participo;
    }

    public void setParticipo(boolean participo) {
        this.participo = participo;
    }


    @Override
    public String toString() {
        return nombre + " (" + peso + " kg, victorias: " + victorias + ")";
    }
}
