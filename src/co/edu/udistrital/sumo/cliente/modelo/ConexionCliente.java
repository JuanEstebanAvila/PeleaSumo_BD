
package co.edu.udistrital.sumo.cliente.modelo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Gestiona la conexion del cliente al servidor via socket.
 * La IP y el puerto se cargan desde el archivo properties
 * para no quemar valores en el codigo.
 *
 * @author Grupo Programacion Avanzada
 */
public class ConexionCliente {

    private Socket socket;
    private String ip;
    private int puerto;
    private PrintWriter salida;
    private BufferedReader entrada;

    /**
     * Construye la conexion con los datos del servidor.
     * No abre el socket hasta que se llame conectar().
     *
     * @param ip     IP o hostname del servidor
     * @param puerto puerto del servidor
     */
    public ConexionCliente(String ip, int puerto) {
        this.ip = ip;
        this.puerto = puerto;
    }

    /**
     * Abre el socket y crea los streams de entrada y salida.
     *
     * @throws IOException si no se puede conectar al servidor
     */
    public void conectar() throws IOException {
        socket  = new Socket(ip, puerto);
        salida  = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * Envia un mensaje al servidor.
     *
     * @param mensaje texto a enviar
     */
    public void enviar(String mensaje) {
        salida.println(mensaje);
    }

    /**
     * Espera y retorna la respuesta del servidor.
     *
     * @return linea de texto recibida
     * @throws IOException si hay error de red
     */
    public String recibirRespuesta() throws IOException {
        return entrada.readLine();
    }

    /**
     * Cierra el socket si esta abierto.
     *
     * @throws IOException si hay error al cerrar
     */
    public void cerrar() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * Indica si el socket esta conectado y abierto.
     *
     * @return true si la conexion esta activa
     */
    public boolean estaConectado() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
