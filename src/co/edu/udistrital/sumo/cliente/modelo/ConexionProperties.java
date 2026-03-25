package co.edu.udistrital.sumo.cliente.modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Carga los kimarites y la configuracion del socket desde archivos properties.
 *
 * El cliente usa DOS archivos properties separados:
 *   - CredencialesClie.properties: IP_SERVIDOR y PUERTO
 *   - kimarites.properties: tecnicas por categoria
 *
 * Solo retorna Strings — no crea objetos del modelo (OCP de SOLID).
 * PROHIBIDO: JFileChooser, logica de negocio, System.out.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionProperties {

    private String ip;
    private int    puerto;

    /**
     * Carga todos los kimarites del archivo properties.
     * Lee todas las claves que contengan "kimarite" en su nombre.
     *
     * @param ruta ruta del archivo properties de kimarites
     * @return lista de nombres de tecnicas
     * @throws IOException si no se puede leer el archivo
     */
    public List<String> cargarKimarites(String ruta) throws IOException {
        List<String> nombres = new ArrayList<>();
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
        }
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
     * Carga la IP y el puerto del servidor desde el properties de credenciales.
     * Ambas claves (IP_SERVIDOR y PUERTO) deben existir en el archivo.
     *
     * @param ruta ruta del archivo properties de credenciales
     * @throws IOException si no se puede leer el archivo o faltan claves
     */
    public void cargarConfiguracion(String ruta) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ruta)) {
            props.load(fis);
        }

        if (!props.containsKey("IP_SERVIDOR")) {
            throw new IOException(
                    "Falta la clave IP_SERVIDOR en el archivo properties");
        }
        ip = props.getProperty("IP_SERVIDOR").trim();

        if (!props.containsKey("PUERTO")) {
            throw new IOException(
                    "Falta la clave PUERTO en el archivo properties");
        }
        try {
            puerto = Integer.parseInt(props.getProperty("PUERTO").trim());
        } catch (NumberFormatException e) {
            throw new IOException("El valor de PUERTO no es un numero valido");
        }
    }

    /** @return IP del servidor cargada del properties */
    public String getIp() { return ip; }

    /** @return puerto del servidor cargado del properties */
    public int getPuerto() { return puerto; }
}
