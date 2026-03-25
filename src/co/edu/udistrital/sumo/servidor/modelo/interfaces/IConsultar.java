package co.edu.udistrital.sumo.servidor.modelo.interfaces;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import java.util.ArrayList;

/**
 * Contrato para consultas a la base de datos.
 * ISP de SOLID: interfaz especifica para operaciones de lectura.
 *
 * @author Grupo Programacion Avanzada
 */
public interface IConsultar {

    Rikishi consultarPorNombre(String nombre);

    ArrayList<Rikishi> consultarTodos();
}
