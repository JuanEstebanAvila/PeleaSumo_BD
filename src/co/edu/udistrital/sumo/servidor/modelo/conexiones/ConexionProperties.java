package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Carga la configuracion del servidor desde el archivo properties.
 * Delega las credenciales de BD a ConexionBD y retorna el puerto del socket.
 *
 * Claves esperadas:
 *   PUERTO=7777
 *   BD.URL=jdbc:mysql://localhost:3306/sumo
 *   BD.USER=root
 *   BD.PASSWORD=
 *
 * PROHIBIDO: System.out, JOptionPane, logica de negocio.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionProperties {

    /**
     * Carga las credenciales de la BD y retorna el puerto del socket.
     * Llama a ConexionBD.cargarCredenciales() internamente.
     *
     * @param ruta ruta del archivo properties del servidor
     * @return puerto del servidor socket
     */
    public static int cargar(String ruta) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
            ConexionBD.cargarCredenciales(ruta);
            return Integer.parseInt(props.getProperty("PUERTO", "7777").trim());
        } catch (NumberFormatException e) {
            return 7777;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar la configuracion del servidor: " + ruta, e);
        }
    }
}
