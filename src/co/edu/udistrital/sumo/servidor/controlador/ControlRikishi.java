package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.dao.RikishiDAO;
import java.util.ArrayList;

/**
 * Controlador de operaciones sobre luchadores (MVC - Controlador de datos).
 * 
 * Sirve de intermediario entre ControlPrincipalS y RikishiDAO.
 * No contiene SQL ni logica de combate — solo delega al DAO.
 * 
 * Principio SRP: solo operaciones de negocio sobre luchadores.
 * No depende de ControlPrincipalS (a diferencia de versiones anteriores).
 * 
 * @author Grupo Programacion Avanzada
 */
public class ControlRikishi {

    /** DAO que encapsula las consultas SQL a la tabla 'luchadores' */
    private final RikishiDAO dao;

    /** Constructor: crea el DAO internamente */
    public ControlRikishi() {
        this.dao = new RikishiDAO();
    }

    /** 
     * Guarda un luchador en la BD.
     * @return true si se guardo exitosamente
     */
    public boolean guardar(Rikishi rikishi) {
        return dao.insertar(rikishi);
    }

    /** Retorna todos los luchadores registrados en la BD */
    public ArrayList<Rikishi> consultarTodos() {
        return dao.consultarTodos();
    }

    /** 
     * Busca un luchador por nombre en la BD.
     * Usado para obtener datos frescos de la BD antes de guardar en RAF.
     * @return Rikishi encontrado o null
     */
    public Rikishi consultarPorNombre(String nombre) {
        return dao.consultarPorNombre(nombre);
    }

    /** 
     * Actualiza el campo 'Combates ganados' en la BD.
     * Se llama despues de cada combate para el ganador.
     */
    public void actualizarVictorias(String nombre, int victorias) {
        dao.actualizarVictorias(nombre, victorias);
    }

    /** 
     * Retorna el ultimo error del DAO para diagnostico.
     * Permite al ControlPrincipalS mostrar por que fallo una operacion.
     */
    public String getUltimoError() {
        return dao.getUltimoError();
    }
}
