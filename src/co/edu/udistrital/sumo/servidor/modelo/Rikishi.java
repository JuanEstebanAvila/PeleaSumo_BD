package co.edu.udistrital.sumo.servidor.modelo;

/**
 * Representa un luchador de sumo (Rikishi).
 * Solo guarda datos — sin logica de combate ni sincronizacion.
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

    public String   getNombre()                  { return nombre; }
    public void     setNombre(String nombre)     { this.nombre = nombre; }

    public double   getPeso()                    { return peso; }
    public void     setPeso(double peso)         { this.peso = peso; }

    public int      getVictorias()               { return victorias; }
    public void     setVictorias(int v)          { this.victorias = v; }

    public String[] getKimarites()               { return kimarites; }
    public void     setKimarites(String[] k)     { this.kimarites = k; }

    public boolean  isDentroDohyo()              { return dentroDohyo; }
    public void     setDentroDohyo(boolean b)    { this.dentroDohyo = b; }

    public String   getRival()                   { return rival; }
    public void     setRival(String rival)       { this.rival = rival; }

    public boolean  isParticipo()                { return participo; }
    public void     setParticipo(boolean p)      { this.participo = p; }

    @Override
    public String toString() {
        return nombre + " (" + peso + " kg, victorias: " + victorias + ")";
    }
}
