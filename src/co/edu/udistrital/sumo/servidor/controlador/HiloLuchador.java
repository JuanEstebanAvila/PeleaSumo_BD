//Copiada del anterior proyecto (ARREGLAR)
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Hilo del servidor que atiende a un luchador conectado por socket.
 *
 * Protocolo:
 *   Cliente -> Servidor: "nombre|peso|k1,k2,k3,..."
 *   Servidor -> Cliente: "GANASTE" o "PERDISTE"
 *   Cliente -> Servidor: "LISTO" (confirmacion de cierre)
 *
 * PROHIBIDO: logica de combate, Dohyo directo, componentes Swing.
 *
 * @author Grupo Programación avanzada
 * @version 2.6
 */
public class HiloLuchador extends Thread {

    private static final int PAUSA_MIN_MS = 300; //Colocamos un mínimo de tiempo de espera para poder apresiar la vista
    private static final int PAUSA_MAX_MS = 500;

    private final Socket       socketCliente;
    // Depende de IArbitro (abstraccion), no de ControladorDohyo (concrecion) — D
    private final IArbitro     arbitro;
    private final int          indice;
    private final CountDownLatch latchCierre;
    private Rikishi rikishi;

    /**
     * @param socketCliente  socket activo del cliente
     * @param arbitro        instancia COMPARTIDA del arbitro del combate (IArbitro)
     * @param indice         posicion del luchador (0 o 1)
     * @param latchCierre    contador compartido con ControladorServidor
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
        // Bandera: true solo cuando se recibio LISTO del cliente correctamente
        boolean listoRecibido = false;

        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socketCliente.getOutputStream(), true)
        ) {
            // Paso 1: Leer datos del luchador
            String lineaDatos = entrada.readLine();
            if (lineaDatos == null || lineaDatos.isEmpty()) return;

            // Paso 2: Construir Rikishi
            rikishi = parsearRikishi(lineaDatos);

            // Paso 3: Subir al dohyo
            arbitro.subirLuchador(rikishi, indice);

            // Paso 4: Esperar al oponente
            arbitro.esperarAmbosLuchadores();

            // Paso 5: Bucle de combate con pausa aleatoria entre turnos
            while (!arbitro.isCombateTerminado()) {
                int pausa = PAUSA_MIN_MS + (int)(Math.random() * (PAUSA_MAX_MS - PAUSA_MIN_MS));
                Thread.sleep(pausa);
                if (!arbitro.isCombateTerminado()) {
                    arbitro.ejecutarTurno(indice);
                }
            }

            // Paso 6: Enviar resultado al cliente
            Rikishi ganador = arbitro.getGanador();
            if (ganador != null && ganador.getNombre().equals(rikishi.getNombre())) {
                salida.println("GANASTE");
            } else {
                salida.println("PERDISTE");
            }

            // Paso 7: Esperar LISTO del cliente (usuario presiono OK en su dialog)
            String confirmacion = entrada.readLine();
            if ("LISTO".equals(confirmacion)) {
                listoRecibido = true;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red: el finally limpia
        } catch (RuntimeException e) {
            // NPE u otro error inesperado: el finally limpia para no colgar el servidor
        } finally {
            cerrarSocket();
            // Se llama siempre para no colgar el latch del servidor.
            // listoRecibido indica si fue flujo normal (true) o error (false).
            latchCierre.countDown();
        }
    }

    /**
     * Parsea "nombre|peso|k1,k2,k3" y construye un Rikishi.
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso = 0.0;
        if (partes.length > 1) {
            try { peso = Double.parseDouble(partes[1].trim()); }
            catch (NumberFormatException ignored) {}
        }
        Rikishi nuevo = new Rikishi(nombre, peso);
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            for (String k : partes[2].trim().split(",")) {
                if (!k.trim().isEmpty())
                    nuevo.getKimarites().add(new Kimarite(k.trim()));
            }
        }
        return nuevo;
    }

    //Cerramos el socket
    private void cerrarSocket() {
        try {
            if (socketCliente != null && !socketCliente.isClosed())
                socketCliente.close();
        } catch (IOException ignored) {}
    }

    public Rikishi getRikishi() { return rikishi; }
}

