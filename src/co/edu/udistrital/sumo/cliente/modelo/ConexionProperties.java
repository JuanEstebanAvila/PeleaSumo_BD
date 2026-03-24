
//Clase pasada del proyecto anterior (sujeta a cambios) le falta leer las credenciales del socket (ip y puerto)
package co.edu.udistrital.sumo.cliente.modelo;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Conexión al archivo de propiedades de kimarites.
 *
 * Propósito: Abrir y parsear el archivo {@code .properties} recibido como
 * parámetro, retornando únicamente Strings crudos para que el controlador
 * construya los objetos {@link Kimarite}.
 * Reside en el modelo porque es una conexión a un archivo.
 * Se comunica con: {@link co.edu.udistrital.sumo.controlador.cliente.ControladorCliente}.
 * Principio SOLID:
 * S — única responsabilidad: leer y parsear el archivo de propiedades.
 * O — no crea objetos del modelo, eso es responsabilidad del controlador.
 *
 * Formato esperado del archivo:
 * kimarite.count=30
 * kimarite1=Yorikiri
 * kimarite2=Hatakikomi
 * ...
 *
 * PROHIBIDO en esta clase: JFileChooser, objetos Kimarite, lógica de negocio,
 * imports del paquete controlador o vista, rutas quemadas.
 *
 * @author Grupo Programación avanzada
 * @version 1.0
 */
public class ConexionProperties {

    //Clave que indica el número total de kimarites en el archivo
    private static final String CLAVE_CONTEO = "kimarite.count";

    //Prefijo de clave para cada kimarite: kimarite1, kimarite2, ...
    private static final String PREFIJO = "kimarite%d";

    /**
     * Carga y retorna la lista de nombres de kimarites desde el archivo
     * en la ruta indicada. Retorna solo Strings crudos — el controlador
     * es quien construye los objetos {@link Kimarite} a partir de estos datos.
     *
     * @param rutaArchivo ruta absoluta del archivo .properties
     *                    (provista por el controlador vía JFileChooser en la Vista)
     * @return lista de nombres crudos, vacía si hay error o archivo inválido
     * @throws IOException si no se puede leer el archivo
     */
    public List<String> cargarNombres(String rutaArchivo) throws IOException {
        List<String> nombres = new ArrayList<>();

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            props.load(fis);
        }

        int count;
        try {
            count = Integer.parseInt(props.getProperty(CLAVE_CONTEO, "0").trim());
        } catch (NumberFormatException e) {
            return nombres; //Archivo mal formado: retorna lista vacía
        }

        for (int i = 1; i <= count; i++) {
            //Lee claves en formato: kimarite1, kimarite2, kimarite3...
            String nombre = props.getProperty(String.format(PREFIJO, i));
            if (nombre != null && !nombre.trim().isEmpty()) {
                nombres.add(nombre.trim());
            }
        }
        return nombres;
    }
}
