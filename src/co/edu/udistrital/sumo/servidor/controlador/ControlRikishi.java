package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.dao.RikishiDAO;
import java.util.ArrayList;

/**
 * Controla las operaciones de negocio sobre los luchadores.
 * Usa RikishiDAO para acceder a la base de datos.
 * Inyeccion de dependencias via constructor (DIP).
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlRikishi {

    private final RikishiDAO      dao;
    private final ControlPrincipalS cp;

    /**
     * Constructor con inyeccion del controlador principal.
     * @param cp controlador principal del servidor
     */
    public ControlRikishi(ControlPrincipalS cp) {
        this.cp  = cp;
        this.dao = new RikishiDAO();
    }

    /**
     * Guarda un luchador en la base de datos.
     * @param rikishi luchador a guardar
     * @return true si se guardo correctamente
     */
    public boolean guardar(Rikishi rikishi) {
        return dao.insertar(rikishi);
    }

    /**
     * Retorna los luchadores que aun no han combatido.
     * @return lista de luchadores disponibles
     */
    public ArrayList<Rikishi> consultarDisponibles() {
        return dao.consultarDisponibles();
    }

    /**
     * Retorna todos los luchadores registrados.
     * @return lista completa
     */
    public ArrayList<Rikishi> consultarTodos() {
        return dao.consultarTodos();
    }

    /**
     * Actualiza las victorias del ganador en la BD.
     * @param nombre    nombre del luchador
     * @param victorias nuevo total de victorias
     */
    public void actualizarVictorias(String nombre, int victorias) {
        dao.actualizarVictorias(nombre, victorias);
    }

    /**
     * Marca ambos luchadores de un combate como participantes.
     * @param nombre1 primer luchador
     * @param nombre2 segundo luchador
     */
    public void marcarParticiparon(String nombre1, String nombre2) {
        dao.marcarParticipo(nombre1);
        dao.marcarParticipo(nombre2);
    }

    /**
     * Retorna el total de luchadores registrados en la BD.
     * @return cantidad total
     */
    public int contarRegistrados() {
        return dao.consultarTodos().size();
    }
}
