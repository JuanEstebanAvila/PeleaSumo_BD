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
 *
 * PROHIBIDO: logica de combate, SQL, Swing.
 *
 * @author Grupo Programacion Avanzada
 */
public class HiloLuchador extends Thread {

    // Pausas mas largas para que el combate se vea en la interfaz
    private static final int PAUSA_MIN = 300;
    private static final int PAUSA_MAX = 600;

    private final Socket            socket;
    private final ControlPrincipalS cp;

    private Rikishi  rikishi;
    private IArbitro dohyo;
    private int      indiceDohyo;
    private String   resultadoFinal;
    private boolean  combatiente = false;

    private final CountDownLatch latchAsignacion = new CountDownLatch(1);
    private CountDownLatch latchFin;

    public HiloLuchador(Socket socket, ControlPrincipalS cp) {
        this.socket = socket;
        this.cp     = cp;
    }

    public void asignarCombate(IArbitro dohyo, int indice,
                               CountDownLatch latchFin) {
        this.dohyo       = dohyo;
        this.indiceDohyo = indice;
        this.latchFin    = latchFin;
        this.combatiente = true;
        latchAsignacion.countDown();
    }

    public void notificarSinCombate(CountDownLatch latchFin) {
        this.resultadoFinal = "SIN_COMBATE";
        this.latchFin       = latchFin;
        latchAsignacion.countDown();
    }

    /** @return true si este hilo fue asignado a un combate */
    public boolean fueCombatiente() {
        return combatiente;
    }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socket.getOutputStream(), true)
        ) {
            // Leer datos del cliente
            String linea = entrada.readLine();
            if (linea == null || linea.isEmpty()) return;

            // Construir Rikishi y registrar
            rikishi = parsearRikishi(linea);
            cp.registrarLuchador(this);

            // Esperar asignacion de combate o SIN_COMBATE
            latchAsignacion.await();

            // Si tiene dohyo, combatir
            if (dohyo != null) {
                dohyo.subirLuchador(rikishi, indiceDohyo);
                dohyo.esperarAmbosLuchadores();

                while (!dohyo.isCombateTerminado()) {
                    int pausa = PAUSA_MIN
                            + (int)(Math.random() * (PAUSA_MAX - PAUSA_MIN));
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

            // Enviar resultado al cliente
            salida.println(resultadoFinal);

            // Esperar LISTO del cliente
            entrada.readLine();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red
        } catch (RuntimeException e) {
            // NPE u otro error
        } finally {
            cerrarSocket();
            if (latchFin != null) latchFin.countDown();
        }
    }

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

    public Rikishi getRikishi() { return rikishi; }
}
