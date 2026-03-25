package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Gestiona el archivo de acceso aleatorio donde se guardan los resultados
 * de cada combate al finalizar. Usa try-with-resources en todas las operaciones.
 *
 * Estructura de cada registro (fija, 116 bytes):
 *   nombre:    50 chars x 2 bytes = 100 bytes
 *   peso:      8 bytes  (double)
 *   victorias: 4 bytes  (int)
 *   resultado: 2 bytes  (char: 'G'=gano, 'P'=perdio)
 *   combate:   2 bytes  (char: numero del combate '1','2','3')
 *
 * PROHIBIDO: System.out, JOptionPane.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionAleatoria {

    private static final String RUTA        = "Data/Servidor/resultados.dat";
    private static final int    TAM_NOMBRE  = 50;
    private static final int    TAM_REG     = TAM_NOMBRE * 2 + 8 + 4 + 2 + 2; // 116

    /**
     * Agrega el resultado de un luchador en el archivo.
     * Los datos deben venir de la base de datos segun el enunciado.
     *
     * @param nombre    nombre del luchador (de la BD)
     * @param peso      peso del luchador (de la BD)
     * @param victorias victorias acumuladas (de la BD)
     * @param gano      true si gano el combate
     * @param numCombate numero del combate (1, 2 o 3)
     * @throws IOException si hay error de escritura
     */
    public static void guardarResultado(String nombre, double peso,
                                         int victorias, boolean gano,
                                         int numCombate) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(RUTA, "rw")) {
            raf.seek(raf.length());
            escribirNombre(raf, nombre);
            raf.writeDouble(peso);
            raf.writeInt(victorias);
            raf.writeChar(gano ? 'G' : 'P');
            raf.writeChar((char)('0' + numCombate));
        }
    }

    /**
     * Lee todos los registros del archivo y los retorna como texto.
     * Usado al finalizar el servidor para mostrar por consola.
     *
     * @return String con todos los registros
     * @throws IOException si hay error de lectura
     */
    public static String leerTodos() throws IOException {
        StringBuilder sb = new StringBuilder();
        try (RandomAccessFile raf = new RandomAccessFile(RUTA, "r")) {
            int total = (int)(raf.length() / TAM_REG);
            for (int i = 0; i < total; i++) {
                raf.seek((long) i * TAM_REG);
                String nombre    = leerNombre(raf);
                double peso      = raf.readDouble();
                int    victorias = raf.readInt();
                char   resultado = raf.readChar();
                char   combate   = raf.readChar();
                sb.append("Combate ").append(combate)
                  .append(" | ").append(nombre)
                  .append(" | Peso: ").append(String.format("%.1f", peso))
                  .append(" kg | Victorias: ").append(victorias)
                  .append(" | ").append(resultado == 'G' ? "GANO" : "PERDIO")
                  .append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Escribe el nombre en exactamente TAM_NOMBRE caracteres (rellena con espacios).
     */
    private static void escribirNombre(RandomAccessFile raf, String nombre) throws IOException {
        StringBuilder sb = new StringBuilder(nombre == null ? "" : nombre);
        while (sb.length() < TAM_NOMBRE) sb.append(' ');
        String fijo = sb.substring(0, TAM_NOMBRE);
        for (char c : fijo.toCharArray()) raf.writeChar(c);
    }

    /**
     * Lee TAM_NOMBRE caracteres y retorna el nombre sin espacios finales.
     */
    private static String leerNombre(RandomAccessFile raf) throws IOException {
        char[] chars = new char[TAM_NOMBRE];
        for (int i = 0; i < TAM_NOMBRE; i++) chars[i] = raf.readChar();
        return new String(chars).trim();
    }
}
