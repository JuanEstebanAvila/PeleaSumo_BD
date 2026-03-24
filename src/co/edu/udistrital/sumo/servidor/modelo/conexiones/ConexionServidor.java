package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase encargada de manejar la conexión del servidor usando sockets.
 *
 * Aquí básicamente se abre el puerto, se esperan las conexiones
 * de los clientes (luchadores) y se devuelven los sockets para que
 * el controlador los use.
 *
 * IMPORTANTE:
 * Esta clase solo maneja la conexión, no tiene nada de lógica del combate.
 * 
 * @author Grupo Programación avanzada
 * @version 1.2
 */
public class ConexionServidor {

    // puerto donde el servidor va a escuchar las conexiones
    private final int puerto;

    // socket principal del servidor (se abre en iniciar y se cierra en cerrar)
    private ServerSocket serverSocket;

    /**
     * Constructor que recibe el puerto donde va a trabajar el servidor.
     * Ojo: aquí no se abre el socket todavía.
     *
     * @param puerto puerto en el que el servidor va a escuchar
     */
    public ConexionServidor(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Abre el ServerSocket en el puerto configurado.
     * Este método se debe llamar antes de aceptar conexiones.
     *
     * @throws IOException si hay error (por ejemplo, el puerto está ocupado)
     */
    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    /**
     * Espera (bloquea el hilo) hasta que un cliente se conecte.
     * Cuando alguien se conecta, devuelve su socket.
     *
     * El controlador usa este método para recibir los dos luchadores.
     *
     * @return socket del cliente que se conectó
     * @throws IOException si ocurre un error en la conexión
     */
    public Socket aceptarConexion() throws IOException {
        return serverSocket.accept();
    }

    /**
     * Cierra el ServerSocket de forma segura.
     * Se usa cuando el servidor ya terminó su trabajo.
     *
     * @throws IOException si ocurre un error al cerrar
     */
    public void cerrar() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    // devuelve el puerto en el que está configurado el servidor
    public int getPuerto() {
        return puerto;
    }

    // indica si el servidor está abierto y listo para recibir conexiones
    public boolean isAbierto() {
        return serverSocket != null && !serverSocket.isClosed();
    }
}
