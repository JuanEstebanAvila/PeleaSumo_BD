package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import java.util.ArrayList;

/**
 * Contrato para consultas a la base de datos.
 * Principio ISP: interfaz especifica para operaciones de lectura.
 *
 * @author Grupo Programacion Avanzada
 */
public interface IConsultar {

    /**
     * Consulta un luchador por su nombre.
     * @param nombre nombre del luchador
     * @return Rikishi encontrado o null
     */
    Rikishi consultarPorNombre(String nombre);

    /**
     * Retorna todos los luchadores de la base de datos.
     * @return lista con todos los luchadores
     */
    ArrayList<Rikishi> consultarTodos();

    /**
     * Retorna los luchadores que aun no han combatido.
     * @return lista de luchadores disponibles
     */
    ArrayList<Rikishi> consultarDisponibles();
}
