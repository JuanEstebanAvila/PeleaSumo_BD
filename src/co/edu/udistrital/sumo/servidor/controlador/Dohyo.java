package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IArbitro;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * El Dohyo ES el monitor de sincronizacion del combate (MVC - Controlador de combate).
 * 
 * Esta es la clase mas critica para la CONCURRENCIA del proyecto.
 * Coordina exactamente dos HiloLuchador usando synchronized + wait/notify.
 * 
 * COMO FUNCIONA EL COMBATE:
 *   1. subirLuchador(): cada hilo registra su Rikishi (se llama 2 veces)
 *   2. esperarAmbosLuchadores(): bloquea hasta que ambos esten listos
 *   3. ejecutarTurno(): cada hilo intenta ejecutar una tecnica en su turno
 *      - Si no es su turno, espera hasta 500ms (MAX_ESPERA_MS)
 *      - Selecciona un kimarite ALEATORIO del arreglo del luchador
 *      - Calcula si expulsa al oponente (5% probabilidad, solo despues de 6 turnos)
 *      - Notifica a los observadores (ControlVistaS -> VistaServidor)
 *   4. Cuando alguien es expulsado, combateTerminado=true y se notifica a todos
 * 
 * PATRON OBSERVER: notifica onKimariteEjecutado() y onCombateTerminado()
 * a ControlVistaS, que actualiza la interfaz grafica en tiempo real.
 * 
 * PATRON DIP (SOLID): HiloLuchador depende de IArbitro (interfaz),
 * no de esta clase concreta.
 * Contiene estado, logica de turnos y metodos synchronized que
 * coordinan exactamente dos HiloLuchador.
 *
 * Implementa IArbitro (DIP): HiloLuchador depende de la abstraccion,
 * no de esta clase concreta.
 *
 * DEBE existir UNA SOLA instancia por combate, compartida entre los dos hilos.
 *
 * PROHIBIDO: sockets, SQL, componentes Swing.
 *
 * @author Grupo Programacion Avanzada
 */
public class Dohyo implements IArbitro {

    public static final int MAX_ESPERA_MS = 500;

    // Probabilidad de expulsion por kimarite (5%)
    // Con pausas de 300-600ms, un combate dura ~10-30 segundos en promedio
    private static final int PROB_EXPULSION = 5;

    // Minimo de turnos antes de que sea posible una expulsion
    // Evita que el combate termine en el primer turno
    private static final int TURNOS_MINIMOS = 6;

    private final Rikishi[] luchadores  = new Rikishi[2];
    private int              turnoActual = 0;
    private int              turnosTotales = 0;
    private volatile boolean combateTerminado  = false;
    private boolean          combateAnunciado  = false;
    private Rikishi          ganador;

    private final Random                   random       = new Random();
    private final List<ICombateObservador> observadores = new ArrayList<>();

    public synchronized void agregarObservador(ICombateObservador obs) {
        if (obs != null) observadores.add(obs);
    }

    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
        rikishi.setDentroDohyo(true);
        notifyAll();
        for (ICombateObservador obs : observadores)
            obs.onLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    @Override
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (luchadores[0] == null || luchadores[1] == null) {
            wait();
        }
        if (!combateAnunciado) {
            combateAnunciado = true;
            luchadores[0].setRival(luchadores[1].getNombre());
            luchadores[1].setRival(luchadores[0].getNombre());
            for (ICombateObservador obs : observadores)
                obs.onCombateIniciado(
                    luchadores[0].getNombre(), luchadores[1].getNombre());
        }
    }

    @Override
    public synchronized void ejecutarTurno(int indiceLuchador)
            throws InterruptedException {
        long inicio = System.currentTimeMillis();

        while (turnoActual != indiceLuchador && !combateTerminado) {
            long restante = MAX_ESPERA_MS - (System.currentTimeMillis() - inicio);
            if (restante <= 0) return;
            wait(restante);
        }

        if (combateTerminado) return;

        Rikishi atacante = luchadores[indiceLuchador];
        if (atacante == null) return;

        if (atacante.getKimarites() == null
                || atacante.getKimarites().length == 0) {
            turnoActual = 1 - indiceLuchador;
            notifyAll();
            return;
        }

        // Seleccionar tecnica aleatoria
        String[] tecnicas = atacante.getKimarites();
        String kimarite   = tecnicas[random.nextInt(tecnicas.length)];

        turnosTotales++;

        // Solo puede haber expulsion despues de los turnos minimos
        boolean expulsado = false;
        if (turnosTotales >= TURNOS_MINIMOS) {
            expulsado = random.nextInt(100) < PROB_EXPULSION;
        }

        if (expulsado) {
            Rikishi oponente = luchadores[1 - indiceLuchador];
            oponente.setDentroDohyo(false);
            atacante.setVictorias(atacante.getVictorias() + 1);
            ganador          = atacante;
            combateTerminado = true;
            notifyAll();
            for (ICombateObservador obs : observadores)
                obs.onKimariteEjecutado(atacante.getNombre(), kimarite, true);
            for (ICombateObservador obs : observadores)
                obs.onCombateTerminado(
                    atacante.getNombre(), atacante.getVictorias());
        } else {
            turnoActual = 1 - indiceLuchador;
            notifyAll();
            for (ICombateObservador obs : observadores)
                obs.onKimariteEjecutado(atacante.getNombre(), kimarite, false);
        }
    }

    @Override
    public synchronized boolean isCombateTerminado() { return combateTerminado; }

    @Override
    public synchronized Rikishi getGanador() { return ganador; }
}
