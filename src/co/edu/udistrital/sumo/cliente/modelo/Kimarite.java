package co.edu.udistrital.sumo.cliente.modelo;

/**
 * Representa una técnica ganadora del sumo.
 *
 * Propósito: Almacenar ÚNICAMENTE el nombre de una técnica.
 * La Asociación Japonesa de Sumo reconoce 82 kimarites oficiales.
 * Cada luchador domina un subconjunto cargado desde el archivo de propiedades.
 * Se comunica con: {@link Rikishi} (quien posee la lista de técnicas).
 * Principio SOLID:
 * S — única responsabilidad: representar el dato de una técnica de sumo.
 *
 * PROHIBIDO en esta clase: lógica de combate, Serializable, interfaz gráfica.
 *
 * @author Grupo Programación avanzada
 * @version 1.6
 * @see Rikishi
 */
public class Kimarite {

    //Nombre oficial de la técnica en japonés
    private String nombre;

    /**
     * Construye un kimarite con su nombre oficial.
     *
     * @param nombre nombre de la técnica
     */
    public Kimarite(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


}
