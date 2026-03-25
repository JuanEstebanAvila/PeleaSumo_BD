package co.edu.udistrital.sumo.cliente.modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Carga los kimarites y la configuracion del socket desde el properties del cliente.
 *
 * El archivo usa formato por categoria:
 *   tecnicasbasicas.kimarite1=Yorikiri
 *   tecnicaslanzamiento.kimarite8=Ipponzeoi
 *   ...
 *   IP_SERVIDOR=localhost
 *   PUERTO=7777
 *
 * Solo retorna Strings — no crea objetos del modelo (OCP de SOLID).
 * PROHIBIDO: JFileChooser, logica de negocio, System.out.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionProperties {

    private String ip     = "localhost";
    private int    puerto = 7777;

    /**
     * Carga todos los kimarites del archivo properties.
     * Lee todas las claves que contengan "kimarite" en su nombre.
     *
     * @param ruta ruta del archivo properties
     * @return lista de nombres de tecnicas
     * @throws IOException si no se puede leer el archivo
     */
    public List<String> cargarKimarites(String ruta) throws IOException {
        List<String> nombres = new ArrayList<>();
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
        }
        // Recorrer todas las claves y tomar las que tienen "kimarite"
        for (String clave : props.stringPropertyNames()) {
            if (clave.contains("kimarite")) {
                String valor = props.getProperty(clave);
                if (valor != null && !valor.trim().isEmpty()) {
                    nombres.add(valor.trim());
                }
            }
        }
        return nombres;
    }

    /**
     * Carga la IP y el puerto del servidor desde el properties.
     * Si no encuentra las claves, conserva los valores por defecto.
     *
     * @param ruta ruta del archivo properties
     * @throws IOException si no se puede leer el archivo
     */
    public void cargarConfiguracion(String ruta) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
        }
        if (props.containsKey("IP_SERVIDOR")) {
            ip = props.getProperty("IP_SERVIDOR").trim();
        }
        if (props.containsKey("PUERTO")) {
            try {
                puerto = Integer.parseInt(props.getProperty("PUERTO").trim());
            } catch (NumberFormatException ignored) {}
        }
    }

    /** @return IP del servidor cargada del properties */
    public String getIp()     { return ip; }

    /** @return puerto del servidor cargado del properties */
    public int    getPuerto() { return puerto; }
}
