package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Gestiona la conexion con la base de datos MySQL del servidor.
 * Las credenciales se cargan desde un archivo properties externo
 * para evitar datos quemados en el codigo.
 *
 * El archivo properties debe contener:
 * db.url, db.usuario, db.contrasena
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionBD {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    /**
     * Carga las credenciales de conexion desde el archivo properties.
     * Debe invocarse antes de cualquier llamada a conectar().
     *
     * @param ruta ruta absoluta del archivo properties
     */
    public static void cargarCredenciales(String ruta) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
            URL= props.getProperty("BD.URL");
            USER= props.getProperty("BD.USER");
            PASSWORD= props.getProperty("BD.PASSWORD");
        } catch (Exception e) {
            throw new RuntimeException("Error al leer credenciales: " + ruta, e);
        }
    }

    /**
     * Abre y retorna una nueva conexion con la base de datos.
     * Requiere que cargarCredenciales() haya sido invocado previamente.
     *
     * @return conexion activa con la base de datos
     */
    public static Connection conectar() {
        if (URL == null ) {
            throw new IllegalStateException("Credenciales no cargadas");
        }
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la base de datos", e);
        }
    }
}
