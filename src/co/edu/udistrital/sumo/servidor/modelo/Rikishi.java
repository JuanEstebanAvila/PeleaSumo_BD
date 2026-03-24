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
    
    //Constructor

    public Rikishi(String nombre, double peso, int combatesGanados, String[] Kimarites) {
        this.nombre = nombre;
        this.peso = peso;
        this.combatesGanados = combatesGanados;
        this.kimarites = Kimarites;
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
    
}
