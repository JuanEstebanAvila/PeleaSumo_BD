package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IArbitro;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * El Dohyo ES el monitor de sincronizacion del combate.
 * Contiene estado, logica de turnos y metodos synchronized que
 * coordinan exactamente dos HiloLuchador.
 *
 * Implementa IArbitro (DIP): HiloLuchador depende de la abstraccion,
 * no de esta clase concreta.
 *
 * DEBE existir UNA SOLA instancia por combate, compartida entre los dos hilos.
 * Si cada hilo tuviera su propio Dohyo, los locks no se comunicarian.
 *
 * PROHIBIDO: sockets, SQL, componentes Swing.
 *
 * @author Grupo Programacion Avanzada
 */
public class Dohyo implements IArbitro {

    // El enunciado exige esperar maximo 500ms entre turnos
    public static final int MAX_ESPERA_MS = 500;

    // Probabilidad de expulsion por kimarite (10%: mayoria resiste, minoria expulsa)
    private static final int PROB_EXPULSION = 10;

    private final Rikishi[] luchadores  = new Rikishi[2];
    private int              turnoActual = 0;
    private volatile boolean combateTerminado  = false;
    private boolean          combateAnunciado  = false;
    private Rikishi          ganador;

    private final Random                   random       = new Random();
    private final List<ICombateObservador> observadores = new ArrayList<>();

    /**
     * Registra un observador para recibir eventos del combate.
     * @param obs observador a registrar
     */
    public synchronized void agregarObservador(ICombateObservador obs) {
        if (obs != null) observadores.add(obs);
    }

    /**
     * Sube el luchador al dohyo, notifica a los observadores y despierta
     * al hilo esperando en esperarAmbosLuchadores().
     * @param rikishi luchador que sube
     * @param indice  posicion 0 o 1
     */
    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        luchadores[indice] = rikishi;
        rikishi.setDentroDohyo(true);
        notifyAll();
        for (ICombateObservador obs : observadores)
            obs.onLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    /**
     * Bloquea el hilo hasta que ambos luchadores esten en el dohyo.
     * Asigna rivales y anuncia el inicio solo una vez.
     * @throws InterruptedException si el hilo es interrumpido
     */
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
                obs.onCombateIniciado(luchadores[0].getNombre(), luchadores[1].getNombre());
        }
    }

    /**
     * Ejecuta el turno del luchador indicado.
     * Espera hasta MAX_ESPERA_MS si no es su turno.
     * Selecciona kimarite al azar y calcula expulsion con baja probabilidad.
     * @param indiceLuchador 0 o 1
     * @throws InterruptedException si el hilo es interrumpido
     */
    @Override
    public synchronized void ejecutarTurno(int indiceLuchador) throws InterruptedException {
        long inicio = System.currentTimeMillis();

        // Esperar hasta que sea el turno de este luchador o el combate termine
        while (turnoActual != indiceLuchador && !combateTerminado) {
            long restante = MAX_ESPERA_MS - (System.currentTimeMillis() - inicio);
            if (restante <= 0) return;
            wait(restante);
        }

        if (combateTerminado) return;

        Rikishi atacante = luchadores[indiceLuchador];
        if (atacante == null) return;

        // Sin tecnicas: ceder turno sin atacar
        if (atacante.getKimarites() == null || atacante.getKimarites().length == 0) {
            turnoActual = 1 - indiceLuchador;
            notifyAll();
            return;
        }

        // Seleccionar tecnica aleatoria del repertorio del luchador
        String[] tecnicas = atacante.getKimarites();
        String kimarite   = tecnicas[random.nextInt(tecnicas.length)];

        // Menor probabilidad de expulsion (90% resiste, 10% expulsa)
        boolean expulsado = random.nextInt(100) < PROB_EXPULSION;

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
                obs.onCombateTerminado(atacante.getNombre(), atacante.getVictorias());
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
