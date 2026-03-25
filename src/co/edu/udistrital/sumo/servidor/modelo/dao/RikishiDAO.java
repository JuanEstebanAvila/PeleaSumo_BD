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

/**
 * Acceso a datos del luchador (Rikishi) en MySQL.
 * Implementa IConsultar e IInsertar (ISP de SOLID).
 * Usa ConexionBD.getInstancia() (Singleton) y try-with-resources.
 *
 * IMPORTANTE: captura Exception (no solo SQLException) porque
 * ConexionBD.conectar() lanza RuntimeException si la BD no esta disponible.
 *
 * Script de la tabla:
 *   CREATE TABLE rikishi (
 *     id INT PRIMARY KEY AUTO_INCREMENT,
 *     nombre VARCHAR(50) NOT NULL,
 *     peso DOUBLE NOT NULL,
 *     victorias INT DEFAULT 0,
 *     kimarites TEXT,
 *     participo BOOLEAN DEFAULT FALSE
 *   );
 *
 * PROHIBIDO: System.out, JOptionPane, logica de negocio.
 *
 * @author Grupo Programacion Avanzada
 */
public class RikishiDAO implements IConsultar, IInsertar {

    private final ConexionBD cnxBD = ConexionBD.getInstancia();

    @Override
    public boolean insertar(Rikishi rikishi) {
        String sql = "INSERT INTO rikishi (nombre, peso, victorias, kimarites, participo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rikishi.getNombre());
            ps.setDouble(2, rikishi.getPeso());
            ps.setInt(3, rikishi.getVictorias());
            ps.setString(4, String.join(",", rikishi.getKimarites()));
            ps.setBoolean(5, false);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Rikishi consultarPorNombre(String nombre) {
        String sql = "SELECT * FROM rikishi WHERE nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rsARikishi(rs);
            }
        } catch (Exception e) {}
        return null;
    }

    @Override
    public ArrayList<Rikishi> consultarTodos() {
        ArrayList<Rikishi> lista = new ArrayList<>();
        String sql = "SELECT * FROM rikishi";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rsARikishi(rs));
        } catch (Exception e) {}
        return lista;
    }

    @Override
    public ArrayList<Rikishi> consultarDisponibles() {
        ArrayList<Rikishi> lista = new ArrayList<>();
        String sql = "SELECT * FROM rikishi WHERE participo = FALSE";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(rsARikishi(rs));
        } catch (Exception e) {}
        return lista;
    }

    public void actualizarVictorias(String nombre, int victorias) {
        String sql = "UPDATE rikishi SET victorias = ? WHERE nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, victorias);
            ps.setString(2, nombre);
            ps.executeUpdate();
        } catch (Exception e) {}
    }

    public boolean incrementarVictorias(String nombre) {
        String sql = "UPDATE rikishi SET victorias = victorias + 1 WHERE nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void marcarParticipo(String nombre) {
        String sql = "UPDATE rikishi SET participo = TRUE WHERE nombre = ?";
        try (Connection con = cnxBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        } catch (Exception e) {}
    }

    private Rikishi rsARikishi(ResultSet rs) throws SQLException {
        String   nombre    = rs.getString("nombre");
        double   peso      = rs.getDouble("peso");
        int      victorias = rs.getInt("victorias");
        String   kmStr     = rs.getString("kimarites");
        String[] kimarites = (kmStr != null && !kmStr.isEmpty())
                             ? kmStr.split(",") : new String[0];
        Rikishi r = new Rikishi(nombre, peso, victorias, kimarites);
        r.setParticipo(rs.getBoolean("participo"));
        return r;
    }
}
