package co.edu.udistrital.sumo.cliente.controlador;

/**
 * Punto de entrada del cliente.
 * 
 * Principio SRP (SOLID): esta clase tiene UNA sola responsabilidad:
 * crear el controlador principal del cliente. No maneja vistas,
 * no abre sockets, no carga archivos.
 * 
 * El metodo main() simplemente instancia ControlPrincipalC, que a su vez
 * se encarga de toda la coordinacion.
 * 
 * @author Grupo Programacion Avanzada
 */
public class LauncherCliente {

    /**
     * Metodo principal. Solo crea el controlador.
     * Al crear ControlPrincipalC:
     *   1. Se abre un JFileChooser para seleccionar CredencialesClie.properties
     *   2. Se carga IP y PUERTO del servidor
     *   3. Se crea la vista del cliente (VistaCliente)
     *   4. Se registran los listeners de los botones
     * 
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        new ControlPrincipalC();
    }
}
