package co.edu.udistrital.sumo.servidor.modelo;

/**
 *Clase encargada de hacer de POJO para los luchadores
 * @author User
 */
public class Rikishi {
    
    //Atributos pertenecientes a cada Rikishi
    private String nombre;
    private double peso;
    private int combatesGanados;
    private String[] kimarites; 
    private Rikishi  rival;
    private boolean  dentroDelDohyo;

    
    //Constructor

    public Rikishi(String nombre, double peso, int combatesGanados, String[] Kimarites) {
        this.nombre = nombre;
        this.peso = peso;
        this.combatesGanados = combatesGanados;
        this.kimarites = Kimarites;
        this.dentroDelDohyo  = false;
    }
    
    //Getters y Setters

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

    public int getCombatesGanados() {
        return combatesGanados;
    }

    public void setCombatesGanados(int combatesGanados) {
        this.combatesGanados = combatesGanados;
    }

    public String[] getKimarites() {
        return kimarites;
    }

    public void setKimarites(String[] Kimarites) {
        this.kimarites = Kimarites;
    }
    
    /**
     * Retorna el luchador rival asignado para el combate actual.
     *
     * @return rival del luchador
     */
    public Rikishi getRival()                  { return rival; }

    /**
     * Asigna el luchador rival para el combate actual.
     *
     * @param rival luchador oponente
     */
    public void setRival(Rikishi rival)        { this.rival = rival; }

    /**
     * Indica si el luchador está dentro del dohyo.
     *
     * @return true si está dentro, false si fue expulsado
     */
    public boolean isDentroDelDohyo()          { return dentroDelDohyo; }

    /**
     * Actualiza el estado del luchador respecto al dohyo.
     */
    public void setDentroDelDohyo(boolean dentro) {
        this.dentroDelDohyo = dentro;
    }
}
    
