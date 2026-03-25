package co.edu.udistrital.sumo.servidor.controlador;

/**
 * Punto de entrada del servidor de combates de sumo.
 * 
 * Principio SRP (SOLID): esta clase tiene UNA sola responsabilidad:
 * instanciar el ControlPrincipalS. Toda la logica de aceptacion de
 * clientes, combates y persistencia se maneja en el controlador.
 * 
 * Al crear ControlPrincipalS:
 *   1. Se abre JFileChooser para seleccionar CredencialesSer.properties
 *   2. Se carga PUERTO, BD.URL, BD.USER, BD.PASSWORD
 *   3. Se crea la VistaServidor (dohyo visual)
 *   4. Se abre el ServerSocket en el puerto configurado
 *   5. Se inicia el hilo principal del servidor que acepta clientes
 * 
 * @author Grupo Programacion Avanzada
 */
public class LauncherServidor {

    public static void main(String[] args) {
        new ControlPrincipalS();
    }
}
