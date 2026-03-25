package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Dohyo;
import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.IArbitro;
import co.edu.udistrital.sumo.servidor.modelo.interfaces.ICombateObservador;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controlador del combate de sumo: lógica de negocio, turnos y sincronización.
 *
 * Es el monitor de sincronización compartido entre los dos HiloLuchador.
 * Todos los métodos públicos son synchronized, garantizando que solo
 * un hilo modifique el estado del dohyo a la vez.
 *
 * Implementa IArbitro (DIP de SOLID): HiloLuchador depende de la
 * abstracción IArbitro, no de esta clase concreta.
 *
 * PROHIBIDO: sockets, SQL, componentes Swing.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 */
public class ControladorDohyo implements IArbitro {

    /** Tiempo máximo de espera por turno en ms */
    public static final int MAX_ESPERA_MS = 500;

    /** Probabilidad de expulsión por kimarite sobre 100 (10%) */
    private static final int PROBABILIDAD_EXPULSION = 10;

    private final Dohyo dohyo;
    private final Random random;
    private final List<ICombateObservador> observadores;

    /**
     * Crea el controlador con el dohyo compartido.
     * Esta instancia DEBE pasarse a ambos HiloLuchador.
     *
     * @param dohyo estado compartido del ring
     */
    public ControladorDohyo(Dohyo dohyo) {
        this.dohyo       = dohyo;
        this.random      = new Random();
        this.observadores = new ArrayList<>();
    }

    /**
     * Registra un observador para recibir eventos del combate.
     *
     * @param obs observador a registrar
     */
    public synchronized void agregarObservador(ICombateObservador obs) {
        if (obs != null) observadores.add(obs);
    }

    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        rikishi.setDentroDelDohyo(true);
        dohyo.setLuchador(rikishi, indice);
        notifyAll();
        notificarLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    @Override
    public synchronized void esperarAmbosLuchadores() throws InterruptedException {
        while (!dohyo.ambosLuchadoresPresentes()) {
            wait();
        }
        if (!dohyo.isCombateAnunciado()) {
            dohyo.setCombateAnunciado(true);
            Rikishi l0 = dohyo.getLuchador(0);
            Rikishi l1 = dohyo.getLuchador(1);
            l0.setRival(l1);
            l1.setRival(l0);
            notificarCombateIniciado(l0.getNombre(), l1.getNombre());
        }
    }

    @Override
    public synchronized void ejecutarTurno(int indiceLuchador)
            throws InterruptedException {

        long inicio = System.currentTimeMillis();

        while (dohyo.getTurnoActual() != indiceLuchador
                && !dohyo.isCombateTerminado()) {
            long restante = MAX_ESPERA_MS - (System.currentTimeMillis() - inicio);
            if (restante <= 0) return;
            wait(restante);
        }

        if (dohyo.isCombateTerminado()) return;

        Rikishi atacante = dohyo.getLuchador(indiceLuchador);
        if (atacante == null) return;

        String kimarite = seleccionarKimariteAleatorio(atacante);
        if (kimarite == null) {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            return;
        }

        boolean expulsado = random.nextInt(100) < PROBABILIDAD_EXPULSION;

        if (expulsado) {
            Rikishi oponente = dohyo.getLuchador(1 - indiceLuchador);
            oponente.setDentroDelDohyo(false);
            atacante.setCombatesGanados(atacante.getCombatesGanados() + 1);
            dohyo.setGanador(atacante);
            dohyo.setCombateTerminado(true);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite, true);
            notificarCombateTerminado(atacante.getNombre(),
                    atacante.getCombatesGanados());
        } else {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite, false);
        }
    }

    @Override
    public synchronized boolean isCombateTerminado() {
        return dohyo.isCombateTerminado();
    }

    @Override
    public synchronized Rikishi getGanador() {
        return dohyo.getGanador();
    }

    /**
     * Selecciona aleatoriamente un kimarite del repertorio del luchador.
     *
     * @param rikishi luchador del que se selecciona la técnica
     * @return nombre del kimarite seleccionado, o null si el repertorio está vacío
     */
    private String seleccionarKimariteAleatorio(Rikishi rikishi) {
        String[] kimarites = rikishi.getKimarites();
        if (kimarites == null || kimarites.length == 0) return null;
        return kimarites[random.nextInt(kimarites.length)];
    }

    // Notificaciones a observadores

    private void notificarLuchadorLlego(String nombre, double peso, int indice) {
        for (ICombateObservador obs : observadores)
            obs.onLuchadorLlego(nombre, peso, indice);
    }

    private void notificarCombateIniciado(String n1, String n2) {
        for (ICombateObservador obs : observadores)
            obs.onCombateIniciado(n1, n2);
    }

    private void notificarKimariteEjecutado(String luchador,
                                             String kimarite,
                                             boolean expulsado) {
        for (ICombateObservador obs : observadores)
            obs.onKimariteEjecutado(luchador, kimarite, expulsado);
    }

    private void notificarCombateTerminado(String ganador, int victorias) {
        for (ICombateObservador obs : observadores)
            obs.onCombateTerminado(ganador, victorias);
    }
}