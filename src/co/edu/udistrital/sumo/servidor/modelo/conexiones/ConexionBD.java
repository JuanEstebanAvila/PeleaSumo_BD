package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexion con la base de datos MySQL/MariaDB (Singleton).
 * Carga las credenciales desde el archivo properties del servidor.
 *
 * PROHIBIDO: System.out, JOptionPane, logica de negocio.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionBD {

    private static ConexionBD instancia;

    private String url;
    private String usuario;
    private String contrasena;
    private boolean driverCargado = false;

    /** Constructor privado — Singleton. */
    private ConexionBD() {}

    /**
     * Retorna la unica instancia de ConexionBD (Singleton).
     * @return instancia unica
     */
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Carga las credenciales desde el archivo properties.
     * Tambien registra el driver JDBC explicitamente.
     * @param ruta ruta del archivo properties del servidor
     */
    public void cargarCredenciales(String ruta) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
            url        = props.getProperty("BD.URL");
            usuario    = props.getProperty("BD.USER");
            contrasena = props.getProperty("BD.PASSWORD");

            // Registrar el driver JDBC explicitamente (requerido por mysql-connector 5.x)
            if (!driverCargado) {
                try {
                    Class.forName("com.mysql.jdbc.Driver");
                    driverCargado = true;
                } catch (ClassNotFoundException e) {
                    // Intentar con el driver nuevo (mysql-connector 8.x)
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        driverCargado = true;
                    } catch (ClassNotFoundException e2) {
                        throw new RuntimeException(
                            "No se encontro el driver JDBC de MySQL. "
                            + "Verifique que mysql-connector-java.jar este en las librerias del proyecto.", e2);
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer las credenciales: " + ruta, e);
        }
    }

    /**
     * Abre y retorna una nueva conexion a la base de datos.
     * @return conexion activa
     */
    public Connection conectar() {
        if (url == null) {
            throw new IllegalStateException(
                    "Credenciales no cargadas. Llame cargarCredenciales() primero.");
        }
        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo conectar a la BD: " + url
                    + " | Error: " + e.getMessage(), e);
        }
    }
}
