package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * Carga la configuracion del servidor desde el archivo properties.
 * Delega las credenciales de BD a ConexionBD (Singleton) y retorna el puerto.
 *
 * Claves esperadas:
 *   PUERTO=7777
 *   BD.URL=jdbc:mysql://localhost:3306/sumo
 *   BD.USER=root
 *   BD.PASSWORD=
 *
 * PROHIBIDO: System.out, JOptionPane, logica de negocio, valores quemados.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionProperties {

    /**
     * Carga las credenciales de la BD y retorna el puerto del socket.
     * Llama a ConexionBD.getInstancia().cargarCredenciales() internamente.
     *
     * @param ruta ruta del archivo properties del servidor
     * @return puerto del servidor socket
     */
    public static int cargar(String ruta) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
            ConexionBD.getInstancia().cargarCredenciales(ruta);

            String puertoProp = props.getProperty("PUERTO");
            if (puertoProp == null || puertoProp.trim().isEmpty()) {
                throw new RuntimeException("Falta la clave PUERTO en: " + ruta);
            }
            return Integer.parseInt(puertoProp.trim());
        } catch (NumberFormatException e) {
            throw new RuntimeException("PUERTO no es un numero valido en: " + ruta, e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo cargar la configuracion del servidor: " + ruta, e);
        }
    }
}
