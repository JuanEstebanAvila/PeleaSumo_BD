package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexion con la base de datos MySQL.
 * Carga las credenciales desde el archivo properties del servidor.
 *
 * Claves esperadas en el properties:
 *   BD.URL=jdbc:mysql://localhost:3306/sumo
 *   BD.USER=root
 *   BD.PASSWORD=
 *
 * PROHIBIDO: System.out, JOptionPane, logica de negocio.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionBD {

    private static String url;
    private static String usuario;
    private static String contrasena;

    /**
     * Carga las credenciales desde el archivo properties.
     * Debe llamarse antes de conectar().
     *
     * @param ruta ruta del archivo properties del servidor
     */
    public static void cargarCredenciales(String ruta) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
            url       = props.getProperty("BD.URL");
            usuario   = props.getProperty("BD.USER");
            contrasena = props.getProperty("BD.PASSWORD");
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer las credenciales: " + ruta, e);
        }
    }

    /**
     * Abre y retorna una nueva conexion a la base de datos.
     * Requiere haber llamado cargarCredenciales() primero.
     *
     * @return conexion activa
     */
    public static Connection conectar() {
        if (url == null) {
            throw new IllegalStateException("Credenciales no cargadas. Llame cargarCredenciales().");
        }
        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }
}
