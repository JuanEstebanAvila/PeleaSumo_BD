package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IArbitro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Hilo del servidor que atiende a un luchador conectado por socket.
 *
 * Protocolo de comunicación:
 *   Cliente -> Servidor : "nombre|peso|k1,k2,k3,..."
 *   Servidor -> Cliente : "GANASTE!!!" o "PERDISTE"
 *   Cliente -> Servidor : "LISTO" (confirmación de cierre)
 *
 * PROHIBIDO: lógica de combate, Dohyo directo, componentes Swing.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 */
public class HiloLuchador extends Thread {

    private static final int PAUSA_MIN_MS = 300;
    private static final int PAUSA_MAX_MS = 500;

    private final Socket         socketCliente;
    private final IArbitro       arbitro;
    private final int            indice;
    private final CountDownLatch latchCierre;
    private Rikishi rikishi;

    /**
     * @param socketCliente socket activo del cliente
     * @param arbitro       instancia COMPARTIDA del árbitro del combate
     * @param indice        posición del luchador en el dohyo (0 o 1)
     * @param latchCierre   contador compartido con ControlPrincipalS
     */
    public HiloLuchador(Socket socketCliente,
                        IArbitro arbitro,
                        int indice,
                        CountDownLatch latchCierre) {
        super("HiloLuchador-" + indice);
        this.socketCliente = socketCliente;
        this.arbitro       = arbitro;
        this.indice        = indice;
        this.latchCierre   = latchCierre;
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socketCliente.getOutputStream(), true)
        ) {
            // Paso 1: Leer datos del luchador enviados por el cliente
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) return;

            // Paso 2: Construir Rikishi con los datos recibidos
            rikishi = parsearRikishi(lineaDatos);

            // Paso 3: Subir al dohyo
            arbitro.subirLuchador(rikishi, indice);

            // Paso 4: Esperar a que el oponente también llegue
            arbitro.esperarAmbosLuchadores();

            // Paso 5: Bucle de combate con pausa aleatoria entre turnos
            while (!arbitro.isCombateTerminado()) {
                int pausa = PAUSA_MIN_MS
                        + (int)(Math.random() * (PAUSA_MAX_MS - PAUSA_MIN_MS));
                Thread.sleep(pausa);
                if (!arbitro.isCombateTerminado()) {
                    arbitro.ejecutarTurno(indice);
                }
            }

            // Paso 6: Enviar resultado al cliente
            Rikishi ganador = (Rikishi) arbitro.getGanador();
            if (ganador != null && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE!!!");
            } else {
                salida.println("PERDISTE");
            }

            // Paso 7: Esperar confirmación "LISTO" del cliente
            String confirmacion = entrada.readLine();
            if (!"LISTO".equals(confirmacion)) {
                System.err.println("HiloLuchador-" + indice
                        + ": confirmación inesperada: " + confirmacion);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            System.err.println("HiloLuchador-" + indice
                    + ": error de red: " + e.getMessage());
        } finally {
            cerrarSocket();
            latchCierre.countDown();
        }
    }

    /**
     * Parsea la línea "nombre|peso|k1,k2,k3" recibida del cliente
     * y construye un objeto Rikishi.
     *
     * @param linea línea de texto recibida por el socket
     * @return Rikishi construido con los datos recibidos
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");

        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";

        double peso = 0.0;
        if (partes.length > 1) {
            try {
                peso = Double.parseDouble(partes[1].trim());
            } catch (NumberFormatException ignored) {}
        }

        String[] kimaritesArr = new String[0];
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            kimaritesArr = partes[2].trim().split(",");
        }

        return new Rikishi(nombre, peso, 0, kimaritesArr);
    }

    /**
     * Cierra el socket del cliente de forma segura.
     */
    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed())
                socketCliente.close();
        } catch (IOException ignored) {}
    }

    /**
     * Retorna el Rikishi atendido por este hilo.
     *
     * @return rikishi del luchador
     */
    public Rikishi getRikishi() { return rikishi; }
}