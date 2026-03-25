package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;

/**
 * Contrato para insercion en la base de datos.
 * Principio ISP: interfaz especifica para operaciones de escritura.
 *
 * @author Grupo Programacion Avanzada
 */
public interface IInsertar {

    /**
     * Inserta un nuevo luchador en la base de datos.
     * @param rikishi luchador a insertar
     * @return true si la insercion fue exitosa
     */
    boolean insertar(Rikishi rikishi);
}
