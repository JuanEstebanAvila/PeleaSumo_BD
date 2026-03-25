package co.edu.udistrital.sumo.servidor.modelo.conexiones;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Gestiona el ServerSocket del servidor.
 * Abre el puerto, acepta conexiones entrantes y las retorna
 * al controlador. No tiene logica de combate.
 *
 * PROHIBIDO: logica de negocio, System.out, JOptionPane.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionServidor {

    private final int    puerto;
    private ServerSocket serverSocket;

    /**
     * Configura el servidor en el puerto indicado.
     * No abre el socket hasta llamar iniciar().
     * @param puerto puerto donde escucha el servidor
     */
    public ConexionServidor(int puerto) {
        this.puerto = puerto;
    }

    /**
     * Abre el ServerSocket en el puerto configurado.
     * @throws IOException si el puerto esta ocupado
     */
    public void iniciar() throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    /**
     * Bloquea hasta que un cliente se conecte y retorna su socket.
     * @return socket del cliente conectado
     * @throws IOException si hay error de red
     */
    public Socket aceptarConexion() throws IOException {
        return serverSocket.accept();
    }

    /**
     * Cierra el ServerSocket de forma segura.
     * @throws IOException si hay error al cerrar
     */
    public void cerrar() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    /** @return true si el servidor esta abierto */
    public boolean isAbierto() {
        return serverSocket != null && !serverSocket.isClosed();
    }

    /** @return el puerto configurado */
    public int getPuerto() { return puerto; }
}
