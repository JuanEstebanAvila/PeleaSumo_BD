//Clase encargada de toda la lógica del combate
package co.edu.udistrital.sumo.controlador.servidor;

import co.edu.udistrital.sumo.modelo.interfaces.IArbitro;
import co.edu.udistrital.sumo.modelo.interfaces.ICombateObservador;
import co.edu.udistrital.sumo.modelo.cliente.Kimarite;
import co.edu.udistrital.sumo.modelo.cliente.Rikishi;
import co.edu.udistrital.sumo.modelo.servidor.Dohyo;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controlador del combate de sumo: logica de negocio, turnos y sincronizacion.
 *
 * Es el monitor de sincronizacion compartido entre los dos HiloLuchador.
 * Todos los metodos publicos son synchronized, garantizando que solo
 * un hilo modifique el estado del dohyo a la vez.
 *
 * Implementa IArbitro (DIP de SOLID): HiloLuchador depende de la
 * abstraccion IArbitro, no de esta clase concreta.
 *
 * Notifica los eventos del combate a los observadores registrados
 * mediante el patron Observer, sin que esta clase conozca la vista.
 *
 * IMPORTANTE: debe existir UNA SOLA instancia compartida entre ambos HiloLuchador.
 *
 * PROHIBIDO: sockets, SQL, componentes Swing.
 *
 * @author Grupo Programacion avanzada
 * @version 3.0
 */
public class ControladorDohyo implements IArbitro {

    // Tiempo maximo de espera por turno en ms (enunciado: maximo 500ms)
    public static final int MAX_ESPERA_MS = 500;

    // Probabilidad de expulsion por kimarite sobre 100
    // 10% — la mayoria de las veces no expulsa, pero en algun momento si
    private static final int PROBABILIDAD_EXPULSION = 10;

    // Estado puro del ring (POJO)
    private final Dohyo dohyo;

    // Generador de numeros aleatorios para kimarites y expulsion
    private final Random random;

    // Lista de observadores (patron Observer)
    private final List<ICombateObservador> observadores;

    /**
     * Crea el controlador con el dohyo compartido.
     * Esta instancia DEBE pasarse a ambos HiloLuchador.
     *
     * @param dohyo estado compartido del ring
     */
    public ControladorDohyo(Dohyo dohyo) {
        this.dohyo        = dohyo;
        this.random       = new Random();
        this.observadores = new ArrayList<>();
    }

    // Registra un observador para recibir eventos del combate
    public synchronized void agregarObservador(ICombateObservador obs) {
        if (obs != null) observadores.add(obs);
    }

    /**
     * Registra al luchador en el dohyo, lo marca como presente
     * y notifica a los observadores de su llegada.
     * notifyAll() despierta al hilo esperando en esperarAmbosLuchadores().
     */
    @Override
    public synchronized void subirLuchador(Rikishi rikishi, int indice) {
        rikishi.setDentroDelDohyo(true);
        dohyo.setLuchador(rikishi, indice);
        notifyAll();
        notificarLuchadorLlego(rikishi.getNombre(), rikishi.getPeso(), indice);
    }

    /**
     * Bloquea el hilo hasta que ambos luchadores esten en el dohyo.
     * La asignacion de rivales y el anuncio del inicio ocurren solo una vez,
     * aunque los dos hilos lleguen aqui casi al mismo tiempo.
     */
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

    /**
     * Ejecuta el turno del luchador indicado:
     * 1. Espera hasta que sea su turno (maximo MAX_ESPERA_MS ms).
     * 2. Selecciona un kimarite aleatorio de su repertorio.
     * 3. Calcula si hay expulsion (PROBABILIDAD_EXPULSION %).
     * 4. Si hay expulsion: registra ganador y notifica fin del combate.
     * 5. Si no: cede el turno al oponente y notifica el kimarite ejecutado.
     */
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

        Kimarite kimarite = seleccionarKimariteAleatorio(atacante);
        if (kimarite == null) {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            return;
        }

        // Numero aleatorio 0-99: expulsion si cae por debajo del umbral
        boolean expulsado = random.nextInt(100) < PROBABILIDAD_EXPULSION;

        if (expulsado) {
            Rikishi oponente = dohyo.getLuchador(1 - indiceLuchador);
            oponente.setDentroDelDohyo(false);
            atacante.setCombatesGanados(atacante.getCombatesGanados() + 1);
            dohyo.setGanador(atacante);
            dohyo.setCombateTerminado(true);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), true);
            notificarCombateTerminado(atacante.getNombre(), atacante.getCombatesGanados());
        } else {
            dohyo.setTurnoActual(1 - indiceLuchador);
            notifyAll();
            notificarKimariteEjecutado(atacante.getNombre(), kimarite.getNombre(), false);
        }
    }

    // Indica si el combate ya termino con un ganador
    @Override
    public synchronized boolean isCombateTerminado() {
        return dohyo.isCombateTerminado();
    }

    // Retorna el luchador ganador, o null si el combate no ha terminado
    @Override
    public synchronized Rikishi getGanador() {
        return dohyo.getGanador();
    }

    // ── Logica de seleccion de kimarite ───────────────────────────────────────

    /**
     * Selecciona aleatoriamente un kimarite del repertorio del luchador.
     *
     * @param rikishi luchador del que se selecciona la tecnica
     * @return kimarite seleccionado, o null si el repertorio esta vacio
     */
    private Kimarite seleccionarKimariteAleatorio(Rikishi rikishi) {
        if (rikishi == null
                || rikishi.getKimarites() == null
                || rikishi.getKimarites().isEmpty()) {
            return null;
        }
        int indice = random.nextInt(rikishi.getKimarites().size());
        return rikishi.getKimarites().get(indice);
    }

    // ── Notificaciones a observadores ─────────────────────────────────────────

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
