package co.edu.udistrital.sumo.servidor.controlador;

/**
 * Punto de entrada del servidor.
 * Solo crea el ControlPrincipalS.
 * No crea objetos del modelo, no maneja vistas directamente.
 *
 * @author Grupo Programacion Avanzada
 */
public class LauncherServidor {

    public static void main(String[] args) {
        new ControlPrincipalS();
    }
}