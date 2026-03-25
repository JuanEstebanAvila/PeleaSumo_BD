package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.dao.RikishiDAO;
import java.util.ArrayList;

/**
 * Controla las operaciones de negocio sobre los luchadores.
 * Usa RikishiDAO para acceder a la base de datos.
 * Sin dependencias innecesarias (DIP de SOLID).
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlRikishi {

    private final RikishiDAO dao;

    public ControlRikishi() {
        this.dao = new RikishiDAO();
    }

    public boolean guardar(Rikishi rikishi) {
        return dao.insertar(rikishi);
    }

    public ArrayList<Rikishi> consultarDisponibles() {
        return dao.consultarDisponibles();
    }

    public ArrayList<Rikishi> consultarTodos() {
        return dao.consultarTodos();
    }

    public Rikishi consultarPorNombre(String nombre) {
        return dao.consultarPorNombre(nombre);
    }

    public void actualizarVictorias(String nombre, int victorias) {
        dao.actualizarVictorias(nombre, victorias);
    }

    public boolean incrementarVictorias(String nombre) {
        return dao.incrementarVictorias(nombre);
    }

    public void marcarParticipo(String nombre) {
        if (nombre != null && !nombre.isEmpty()) {
            dao.marcarParticipo(nombre);
        }
    }

    public int contarRegistrados() {
        return dao.consultarTodos().size();
    }
}
