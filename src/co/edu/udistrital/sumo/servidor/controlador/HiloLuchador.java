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
 * Hilo del servidor que atiende a un cliente conectado.
 * Flujo:
 *   1. Lee datos del cliente: "nombre|peso|k1,k2,..."
 *   2. Notifica al ControlPrincipalS para guardar en BD.
 *   3. Espera ser asignado a un combate (latchAsignacion).
 *   4. Si tiene Dohyo asignado: combate en el Dohyo.
 *   5. Envia "GANASTE" o "PERDISTE" al cliente.
 *   6. Espera "LISTO" y notifica al ControlPrincipalS que termino.
 *
 * PROHIBIDO: logica de combate, SQL, Swing.
 *
 * @author Grupo Programacion Avanzada
 */
public class HiloLuchador extends Thread {

    // Pausa aleatoria entre turnos (enunciado: esperas aleatorias, max 500ms)
    private static final int PAUSA_MIN = 100;
    private static final int PAUSA_MAX = 400;

    private final Socket           socket;
    private final ControlPrincipalS cp;

    private Rikishi  rikishi;
    private IArbitro dohyo;
    private int      indiceDohyo;
    private String   resultadoFinal;  // "GANASTE" o "PERDISTE"

    // El ControlPrincipalS llama asignarCombate() o notificarSinCombate()
    // para despertar a este hilo despues de registrarlo en la BD
    private final CountDownLatch latchAsignacion = new CountDownLatch(1);

    // Latch que ControlPrincipalS espera para saber cuando este hilo termino
    private CountDownLatch latchFin;

    /**
     * Crea el hilo para atender al cliente conectado.
     * @param socket socket activo con el cliente
     * @param cp     controlador principal del servidor
     */
    public HiloLuchador(Socket socket, ControlPrincipalS cp) {
        this.socket = socket;
        this.cp     = cp;
    }

    /**
     * Asigna un Dohyo a este hilo para que participe en un combate.
     * Despierta al hilo que espera en latchAsignacion.
     * @param dohyo    dohyo del combate
     * @param indice   posicion 0 o 1
     * @param latchFin latch que decrementar al terminar
     */
    public void asignarCombate(IArbitro dohyo, int indice, CountDownLatch latchFin) {
        this.dohyo       = dohyo;
        this.indiceDohyo = indice;
        this.latchFin    = latchFin;
        latchAsignacion.countDown();
    }

    /**
     * Notifica a este hilo que no combatira.
     * Le envia "SIN_COMBATE" al cliente para que cierre.
     * @param latchFin latch que decrementar al terminar
     */
    public void notificarSinCombate(CountDownLatch latchFin) {
        this.resultadoFinal = "SIN_COMBATE";
        this.latchFin       = latchFin;
        latchAsignacion.countDown();
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Paso 1: leer datos del cliente
            String linea = entrada.readLine();
            if (linea == null || linea.isEmpty()) return;

            // Paso 2: construir Rikishi y notificar al controlador para guardar en BD
            rikishi = parsearRikishi(linea);
            cp.registrarLuchador(this);

            // Paso 3: esperar asignacion de combate o resultado directo
            latchAsignacion.await();

            // Paso 4: si tiene dohyo, combatir
            if (dohyo != null) {
                dohyo.subirLuchador(rikishi, indiceDohyo);
                dohyo.esperarAmbosLuchadores();

                while (!dohyo.isCombateTerminado()) {
                    int pausa = PAUSA_MIN + (int)(Math.random() * (PAUSA_MAX - PAUSA_MIN));
                    Thread.sleep(pausa);
                    if (!dohyo.isCombateTerminado()) {
                        dohyo.ejecutarTurno(indiceDohyo);
                    }
                }

                Rikishi ganador = dohyo.getGanador();
                resultadoFinal = (ganador != null
                    && ganador.getNombre().equals(rikishi.getNombre()))
                    ? "GANASTE" : "PERDISTE";
            }

            // Paso 5: enviar resultado al cliente
            salida.println(resultadoFinal);

            // Paso 6: esperar confirmacion LISTO del cliente
            entrada.readLine();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red: el finally libera el latch
        } catch (RuntimeException e) {
            // NPE u otro error: el finally libera el latch
        } finally {
            cerrarSocket();
            if (latchFin != null) latchFin.countDown();
        }
    }

    /**
     * Parsea "nombre|peso|k1,k2,k3" y construye el Rikishi.
     */
    private Rikishi parsearRikishi(String linea) {
        String[] partes = linea.split("\\|");
        String nombre = partes.length > 0 ? partes[0].trim() : "Desconocido";
        double peso   = 0.0;
        if (partes.length > 1) {
            try { peso = Double.parseDouble(partes[1].trim()); }
            catch (NumberFormatException ignored) {}
        }
        String[] kimarites = new String[0];
        if (partes.length > 2 && !partes[2].trim().isEmpty()) {
            kimarites = partes[2].trim().split(",");
            // limpiar espacios de cada kimarite
            for (int i = 0; i < kimarites.length; i++)
                kimarites[i] = kimarites[i].trim();
        }
        return new Rikishi(nombre, peso, 0, kimarites);
    }

    private void cerrarSocket() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    /** @return el Rikishi de este hilo */
    public Rikishi getRikishi() { return rikishi; }
}
