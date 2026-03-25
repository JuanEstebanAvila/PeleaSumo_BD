package co.edu.udistrital.sumo.servidor.modelo.dao;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionBD;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IConsultar;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IInsertar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Acceso a datos del luchador en MySQL (MVC - Modelo, Patron DAO).
 * 
 * PATRON DAO (Data Access Object):
 *   Encapsula TODA la logica de acceso a la base de datos.
 *   Las demas clases no conocen SQL ni JDBC — solo usan este DAO.
 * 
 * PATRON ISP (Interface Segregation Principle):
 *   Implementa IConsultar (solo lecturas) e IInsertar (solo escrituras).
 *   Un cliente que solo necesita consultar no depende de insertar().
 * 
 * PATRON SINGLETON (indirecto):
 *   Usa ConexionBD.getInstancia() para obtener la conexion.
 * 
 * TABLA REAL EN MySQL (creada desde phpMyAdmin):
 *   luchadores(ID VARCHAR(20) PK, Nombre VARCHAR(100),
 *              Peso DECIMAL(6,0), `Combates ganados` VARCHAR(20),
 *              `Kimarites experto` VARCHAR(1200))
 * 
 * NOTA: las columnas con espacios usan backticks en SQL.
 * No existe columna 'participo' — eso se controla en memoria.
 *
 * TABLA REAL en la BD (phpMyAdmin):
 *   CREATE TABLE luchadores (
 *     Nombre VARCHAR(100) NOT NULL,
 *     Peso DECIMAL(6,0) NOT NULL,
 *     `Combates ganados` VARCHAR(20) NOT NULL,
 *     `Kimarites experto` VARCHAR(1200) NOT NULL,
 *     ID VARCHAR(20) NOT NULL PRIMARY KEY
 *   );
 *
 * Guarda el ultimo error para diagnostico desde el controlador.
 *
 * @author Grupo Programacion Avanzada
 */
public class RikishiDAO implements IConsultar, IInsertar {

    private final ConexionBD cnxBD = ConexionBD.getInstancia();

    // Ultimo error para que el controlador pueda mostrar que fallo
    private String ultimoError = "";

    /** @return descripcion del ultimo error ocurrido */
    public String getUltimoError() { return ultimoError; }

    @Override
    public boolean insertar(Rikishi rikishi) {
        String sql = "INSERT INTO luchadores (ID, Nombre, Peso, `Combates ganados`, `Kimarites experto`) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, generarId());
            ps.setString(2, rikishi.getNombre());
            ps.setDouble(3, rikishi.getPeso());
            ps.setString(4, String.valueOf(rikishi.getVictorias()));
            ps.setString(5, String.join(",", rikishi.getKimarites()));
            ps.executeUpdate();
            ultimoError = "";
            return true;
        } catch (Exception e) {
            ultimoError = e.getMessage();
            return false;
        }
    }

    @Override
    public Rikishi consultarPorNombre(String nombre) {
        String sql = "SELECT * FROM luchadores WHERE Nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rsARikishi(rs);
            }
        } catch (Exception e) {
            ultimoError = e.getMessage();
        }
        return null;
    }

    @Override
    public ArrayList<Rikishi> consultarTodos() {
        ArrayList<Rikishi> lista = new ArrayList<>();
        String sql = "SELECT * FROM luchadores";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rsARikishi(rs));
        } catch (Exception e) {
            ultimoError = e.getMessage();
        }
        return lista;
    }

    public void actualizarVictorias(String nombre, int victorias) {
        String sql = "UPDATE luchadores SET `Combates ganados` = ? WHERE Nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, String.valueOf(victorias));
            ps.setString(2, nombre);
            ps.executeUpdate();
        } catch (Exception e) {
            ultimoError = e.getMessage();
        }
    }

    private String generarId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    private Rikishi rsARikishi(ResultSet rs) throws SQLException {
        String nombre    = rs.getString("Nombre");
        double peso      = rs.getDouble("Peso");
        String vicStr    = rs.getString("Combates ganados");
        int    victorias = 0;
        try { victorias = Integer.parseInt(vicStr.trim()); }
        catch (NumberFormatException ignored) {}
        String   kmStr     = rs.getString("Kimarites experto");
        String[] kimarites = (kmStr != null && !kmStr.isEmpty())
                             ? kmStr.split(",") : new String[0];
        return new Rikishi(nombre, peso, victorias, kimarites);
    }
}
