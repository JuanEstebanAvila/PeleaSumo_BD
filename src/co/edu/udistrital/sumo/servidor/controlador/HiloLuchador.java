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
 * El ganador NO cierra su conexion despues de un combate.
 * Se queda BLOQUEADO en esperarSiguienteOrden() hasta que
 * ControlPrincipalS le asigne otro combate o le envie resultado final.
 *
 * Sincronizacion via monitor (wait/notify) para evitar race conditions
 * entre el hilo del luchador y el hilo principal del servidor.
 *
 * PROHIBIDO: logica de combate, SQL, Swing.
 *
 * @author Grupo Programacion Avanzada
 */
public class HiloLuchador extends Thread {

    private static final int PAUSA_MIN = 300;
    private static final int PAUSA_MAX = 500;

    private final Socket            socket;
    private final ControlPrincipalS cp;

    private Rikishi  rikishi;
    private boolean  combatiente = false;

    // === Estado del combate actual (protegido por 'monitor') ===
    private final Object monitor = new Object();

    // Orden pendiente del servidor: NINGUNA, COMBATIR, o RESULTADO_FINAL
    private enum Orden { NINGUNA, COMBATIR, RESULTADO_FINAL }
    private Orden ordenPendiente = Orden.NINGUNA;

    // Datos del combate asignado
    private IArbitro dohyo;
    private int      indiceDohyo;
    private CountDownLatch latchCombateFin;

    // Resultado final para enviar al cliente
    private String resultadoFinal;

    public HiloLuchador(Socket socket, ControlPrincipalS cp) {
        this.socket = socket;
        this.cp     = cp;
    }

    /**
     * Asigna un combate a este hilo. Despierta al hilo si esta bloqueado.
     * Puede llamarse multiples veces si el luchador sigue ganando.
     */
    public void asignarCombate(IArbitro dohyo, int indice,
                               CountDownLatch latchCombateFin) {
        synchronized (monitor) {
            this.dohyo           = dohyo;
            this.indiceDohyo     = indice;
            this.latchCombateFin = latchCombateFin;
            this.combatiente     = true;
            this.ordenPendiente  = Orden.COMBATIR;
            monitor.notifyAll();
        }
    }

    /**
     * Envia el resultado final al cliente.
     * Llamado por ControlPrincipalS cuando el luchador ya no combatira mas.
     * Despierta al hilo para que envie el resultado y espere LISTO.
     */
    public void enviarResultadoFinal(String resultado) {
        synchronized (monitor) {
            this.resultadoFinal = resultado;
            this.ordenPendiente = Orden.RESULTADO_FINAL;
            monitor.notifyAll();
        }
    }

    /**
     * Notifica que este luchador no combatira. Equivale a enviar SIN_COMBATE.
     */
    public void notificarSinCombate(CountDownLatch latchFin) {
        this.latchCombateFin = latchFin;
        enviarResultadoFinal("SIN_COMBATE");
    }

    public boolean fueCombatiente() { return combatiente; }

    @Override
    public void run() {
        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter salida = new PrintWriter(
                socket.getOutputStream(), true)
        ) {
            // Paso 1: leer datos del cliente
            String linea = entrada.readLine();
            if (linea == null || linea.isEmpty()) return;

            // Paso 2: construir Rikishi y registrar en BD
            rikishi = parsearRikishi(linea);
            cp.registrarLuchador(this);

            // Paso 3: esperar primera orden del servidor
            Orden orden = esperarOrden();

            // Paso 4: loop de combates (se repite si gana)
            while (orden == Orden.COMBATIR) {
                ejecutarCombate();
                // Despues de combatir, esperar siguiente orden:
                // - COMBATIR de nuevo (si gano y hay mas combates)
                // - RESULTADO_FINAL (si perdio o ya termino el torneo)
                orden = esperarOrden();
            }

            // Paso 5: enviar resultado final al cliente
            salida.println(resultadoFinal);

            // Paso 6: esperar LISTO del cliente
            entrada.readLine();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Error de red
        } catch (RuntimeException e) {
            // NPE u otro error
        } finally {
            cerrarSocket();
            if (latchCombateFin != null) latchCombateFin.countDown();
        }
    }

    /**
     * Bloquea el hilo hasta que ControlPrincipalS le de una orden.
     * Usa synchronized + wait() para evitar race conditions.
     *
     * @return la orden recibida (COMBATIR o RESULTADO_FINAL)
     */
    private Orden esperarOrden() throws InterruptedException {
        synchronized (monitor) {
            while (ordenPendiente == Orden.NINGUNA) {
                monitor.wait();
            }
            Orden orden = ordenPendiente;
            ordenPendiente = Orden.NINGUNA; // resetear para la siguiente espera
            return orden;
        }
    }

    /**
     * Ejecuta el combate en el dohyo asignado.
     */
    private void ejecutarCombate() throws InterruptedException {
        IArbitro dohyoLocal;
        int indiceLocal;
        CountDownLatch latchLocal;

        // Copiar referencias bajo el monitor
        synchronized (monitor) {
            dohyoLocal  = this.dohyo;
            indiceLocal = this.indiceDohyo;
            latchLocal  = this.latchCombateFin;
        }

        dohyoLocal.subirLuchador(rikishi, indiceLocal);
        dohyoLocal.esperarAmbosLuchadores();

        while (!dohyoLocal.isCombateTerminado()) {
            int pausa = PAUSA_MIN
                    + (int)(Math.random() * (PAUSA_MAX - PAUSA_MIN));
            Thread.sleep(pausa);
            if (!dohyoLocal.isCombateTerminado()) {
                dohyoLocal.ejecutarTurno(indiceLocal);
            }
        }

        // Notificar que este lado del combate termino
        if (latchLocal != null) {
            latchLocal.countDown();
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
