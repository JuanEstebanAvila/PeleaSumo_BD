package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;

/**
 * Contrato para insercion en la base de datos.
 * ISP de SOLID: interfaz especifica para operaciones de escritura.
 *
 * @author Grupo Programacion Avanzada
 */
public interface IInsertar {

    boolean insertar(Rikishi rikishi);
}
