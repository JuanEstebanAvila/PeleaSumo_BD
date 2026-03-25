package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionAleatoria;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionProperties;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionServidor;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Controlador principal del servidor.
 *
 * Flujo segun enunciado:
 *   1. Acepta exactamente 6 conexiones de clientes.
 *   2. Registra cada luchador en la BD.
 *   3. Selecciona 2 aleatorios para el primer combate.
 *   4. El ganador se enfrenta al siguiente aleatorio de los pendientes.
 *   5. Repite hasta que TODOS los luchadores hayan combatido (5 combates).
 *   6. Datos del RAF vienen de consulta a la BD.
 *   7. El campo G/P lo agrega el servidor.
 *
 * CLAVE: la seleccion de combatientes usa una LISTA EN MEMORIA
 * (luchadores pendientes), NO depende de consultarDisponibles() de la BD.
 * La BD se usa para persistencia y para consultar datos del RAF.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalS {

    private static final int TOTAL_LUCHADORES = 6;

    // Con 6 luchadores en formato carry-over: 5 combates
    // Combate 1: A vs B, Combate 2: ganador vs C, ... Combate 5: ganador vs F
    private static final int NUM_COMBATES = TOTAL_LUCHADORES - 1;

    private final ControlVistaS    controlVista;
    private final ControlRikishi   controlRikishi;
    private final ConexionServidor cnxServidor;

    private final List<HiloLuchador> hilosConectados = new ArrayList<>();

    // Lista EN MEMORIA de luchadores pendientes (no depende de la BD)
    private final List<Rikishi> pendientes = new ArrayList<>();

    private volatile int contadorConectados = 0;

    public ControlPrincipalS() {
        String rutaProperties = seleccionarProperties();
        if (rutaProperties == null) {
            System.exit(0);
            this.controlVista   = null;
            this.controlRikishi = null;
            this.cnxServidor    = null;
            return;
        }

        int puerto = ConexionProperties.cargar(rutaProperties);
        this.controlVista   = new ControlVistaS();
        this.controlRikishi = new ControlRikishi();
        this.cnxServidor    = new ConexionServidor(puerto);

        iniciar();
    }

    private String seleccionarProperties() {
        JFileChooser fc = new JFileChooser(new File("Data/Servidor/"));
        fc.setFileFilter(new FileNameExtensionFilter(
                "Archivo de propiedades", "properties"));
        fc.setDialogTitle("Seleccione el properties del servidor");
        int resultado = fc.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private void iniciar() {
        Thread hiloServidor = new Thread(this::ejecutar, "HiloServidorPrincipal");
        hiloServidor.setDaemon(false);
        hiloServidor.start();
    }

    private void ejecutar() {
        try {
            cnxServidor.iniciar();
            controlVista.mostrar("Servidor iniciado. Esperando luchadores...");
            controlVista.actualizarEstado(
                    "Esperando " + TOTAL_LUCHADORES + " luchadores...");

            // Aceptar exactamente 6 conexiones
            while (contadorConectados < TOTAL_LUCHADORES) {
                Socket socket = cnxServidor.aceptarConexion();
                controlVista.mostrar("Cliente conectado: "
                        + socket.getInetAddress().getHostAddress());

                HiloLuchador hilo = new HiloLuchador(socket, this);
                hilosConectados.add(hilo);
                hilo.start();

                Thread.sleep(1000);
            }

            cnxServidor.cerrar();
            controlVista.mostrar(
                    TOTAL_LUCHADORES + " luchadores conectados. Iniciando torneo...");
            controlVista.actualizarEstado("Todos conectados. Preparando combates...");

            Thread.sleep(2000);

            // Limpiar archivo RAF de torneos anteriores
            ConexionAleatoria.limpiar();

            // Preparar lista de pendientes en memoria (copia de los registrados)
            synchronized (this) {
                pendientes.clear();
                for (HiloLuchador h : hilosConectados) {
                    if (h.getRikishi() != null) {
                        pendientes.add(h.getRikishi());
                    }
                }
            }

            // Mezclar aleatoriamente
            Collections.shuffle(pendientes);

            controlVista.mostrar("Luchadores en el torneo: " + pendientes.size());

            // Ejecutar todos los combates hasta agotar pendientes
            String nombreGanadorActual = null;
            for (int numCombate = 1; numCombate <= NUM_COMBATES; numCombate++) {
                nombreGanadorActual = ejecutarCombate(numCombate, nombreGanadorActual);
                if (nombreGanadorActual == null) break;
                if (numCombate < NUM_COMBATES) {
                    Thread.sleep(3000); // pausa entre combates
                }
            }

            // Mostrar campeon
            if (nombreGanadorActual != null) {
                controlVista.mostrar("=== CAMPEON DEL TORNEO: "
                        + nombreGanadorActual + " ===");
            }

            // Mostrar contenido del RAF por consola
            Thread.sleep(1000);
            controlVista.mostrar("=== Leyendo archivo de acceso aleatorio ===");
            try {
                String contenidoRAF = ConexionAleatoria.leerTodos();
                if (contenidoRAF != null && !contenidoRAF.isEmpty()) {
                    controlVista.mostrar(contenidoRAF);
                    System.out.println("\n=== RESULTADOS DEL TORNEO (RAF) ===");
                    System.out.println(contenidoRAF);
                    System.out.println("====================================");
                } else {
                    controlVista.mostrar("El archivo RAF esta vacio.");
                    System.out.println("El archivo RAF esta vacio.");
                }
            } catch (IOException e) {
                controlVista.mostrar("Error al leer RAF: " + e.getMessage());
            }

            // Notificar a clientes que no combatieron
            notificarSinCombate();

            Thread.sleep(3000);
            controlVista.cerrar();

        } catch (IOException e) {
            controlVista.mostrar("Error del servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ejecuta un combate. Usa la lista EN MEMORIA de pendientes,
     * NO depende de consultarDisponibles() de la BD.
     */
    private String ejecutarCombate(int numCombate, String nombreGanadorAnt)
            throws IOException, InterruptedException {

        controlVista.mostrar("=== Preparando Combate " + numCombate + " ===");
        controlVista.resetearSeleccion();

        Rikishi luchador1;
        Rikishi luchador2;

        if (nombreGanadorAnt != null) {
            // El ganador anterior vs el siguiente pendiente
            luchador1 = buscarRikishiEnMemoria(nombreGanadorAnt);
            if (luchador1 == null) {
                controlVista.mostrar("Error: no se encontro al ganador anterior.");
                return null;
            }
            if (pendientes.isEmpty()) {
                controlVista.mostrar("No hay mas pendientes para combatir.");
                return nombreGanadorAnt;
            }
            luchador2 = pendientes.remove(0); // tomar el siguiente pendiente
        } else {
            // Primer combate: tomar 2 de los pendientes
            if (pendientes.size() < 2) {
                controlVista.mostrar("No hay suficientes luchadores.");
                return null;
            }
            luchador1 = pendientes.remove(0);
            luchador2 = pendientes.remove(0);
        }

        // Mostrar seleccion en grilla
        controlVista.mostrarSeleccionCombatientes(
                luchador1.getNombre(), luchador1.getPeso(),
                luchador2.getNombre(), luchador2.getPeso());

        Thread.sleep(2000);

        controlVista.mostrar("Combate " + numCombate + ": "
                + luchador1.getNombre() + " vs " + luchador2.getNombre());

        controlVista.mostrarLuchadorEnDohyo(
                luchador1.getNombre(), luchador1.getPeso(), 0);
        controlVista.mostrarLuchadorEnDohyo(
                luchador2.getNombre(), luchador2.getPeso(), 1);
        controlVista.mostrarInicioCombate(
                luchador1.getNombre(), luchador2.getNombre());

        // Encontrar los hilos
        HiloLuchador hilo1 = buscarHilo(luchador1.getNombre());
        HiloLuchador hilo2 = buscarHilo(luchador2.getNombre());

        if (hilo1 == null || hilo2 == null) {
            controlVista.mostrar(
                    "Error: no se encontraron los hilos para el combate.");
            return nombreGanadorAnt;
        }

        // Crear Dohyo y registrar observador
        Dohyo dohyo = new Dohyo();
        dohyo.agregarObservador(controlVista);

        // Latch para esperar que ambos hilos terminen
        CountDownLatch latchCombate = new CountDownLatch(2);

        // Asignar combate (despierta a los hilos)
        hilo1.asignarCombate(dohyo, 0, latchCombate);
        hilo2.asignarCombate(dohyo, 1, latchCombate);

        // Esperar a que terminen
        latchCombate.await();

        // Determinar ganador
        Rikishi ganador  = dohyo.getGanador();
        if (ganador == null) {
            controlVista.mostrar("Error: el combate no tuvo ganador.");
            return null;
        }

        Rikishi perdedor = ganador.getNombre().equals(luchador1.getNombre())
                           ? luchador2 : luchador1;

        controlVista.mostrar("GANADOR del combate " + numCombate + ": "
                + ganador.getNombre() + " (victorias: " + ganador.getVictorias() + ")");

        // Actualizar BD: victorias del ganador
        controlRikishi.actualizarVictorias(
                ganador.getNombre(), ganador.getVictorias());

        // Mostrar ganador en la vista
        controlVista.mostrarGanador(ganador.getNombre(), ganador.getVictorias());

        // Marcar perdedor como participante en BD
        controlRikishi.marcarParticipo(perdedor.getNombre());

        // Marcar perdedor en la grilla
        controlVista.marcarLuchadorParticipo(perdedor.getNombre());

        // Guardar en RAF: datos de la BD si es posible, sino de memoria
        guardarEnRAF(ganador, perdedor, numCombate);

        return ganador.getNombre();
    }

    /**
     * Guarda los resultados en el archivo de acceso aleatorio.
     * Intenta obtener los datos de la BD (como exige el enunciado).
     * Si la BD falla, usa los datos en memoria como respaldo.
     */
    private void guardarEnRAF(Rikishi ganador, Rikishi perdedor,
                              int numCombate) {
        // Intentar consultar datos de la BD (enunciado: datos del RAF vienen de la BD)
        Rikishi ganadorBD  = controlRikishi.consultarPorNombre(ganador.getNombre());
        Rikishi perdedorBD = controlRikishi.consultarPorNombre(perdedor.getNombre());

        // Si la BD no retorna datos, usar los de memoria
        Rikishi gFinal = ganadorBD  != null ? ganadorBD  : ganador;
        Rikishi pFinal = perdedorBD != null ? perdedorBD : perdedor;

        try {
            // El campo G/P lo agrega el servidor (no viene de la BD)
            ConexionAleatoria.guardarResultado(
                    gFinal.getNombre(), gFinal.getPeso(),
                    ganador.getVictorias(), true, numCombate);
            ConexionAleatoria.guardarResultado(
                    pFinal.getNombre(), pFinal.getPeso(),
                    perdedor.getVictorias(), false, numCombate);
            controlVista.mostrar("Resultados del combate " + numCombate
                    + " guardados en RAF.");
        } catch (IOException e) {
            controlVista.mostrar("Error al guardar en RAF: " + e.getMessage());
        }
    }

    /**
     * Busca un Rikishi en memoria (hilosConectados), no en la BD.
     */
    private Rikishi buscarRikishiEnMemoria(String nombre) {
        for (HiloLuchador h : hilosConectados) {
            if (h.getRikishi() != null
                    && h.getRikishi().getNombre().equals(nombre)) {
                return h.getRikishi();
            }
        }
        return null;
    }

    /**
     * Notifica SIN_COMBATE a los hilos que nunca fueron asignados.
     */
    private void notificarSinCombate() {
        List<HiloLuchador> sinAsignar = new ArrayList<>();
        for (HiloLuchador hilo : hilosConectados) {
            if (hilo.isAlive() && !hilo.fueCombatiente()) {
                sinAsignar.add(hilo);
            }
        }
        if (sinAsignar.isEmpty()) return;

        CountDownLatch latchRestantes = new CountDownLatch(sinAsignar.size());
        for (HiloLuchador hilo : sinAsignar) {
            String nombre = hilo.getRikishi() != null
                    ? hilo.getRikishi().getNombre() : "?";
            controlVista.mostrar("Notificando SIN_COMBATE a: " + nombre);
            hilo.notificarSinCombate(latchRestantes);
        }
        try {
            latchRestantes.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private HiloLuchador buscarHilo(String nombre) {
        for (HiloLuchador h : hilosConectados) {
            if (h.getRikishi() != null
                    && h.getRikishi().getNombre().equals(nombre)) {
                return h;
            }
        }
        return null;
    }

    /**
     * Llamado por HiloLuchador al recibir datos del cliente.
     * Actualiza la grilla PRIMERO, luego intenta guardar en BD.
     */
    public synchronized void registrarLuchador(HiloLuchador hilo) {
        Rikishi rikishi = hilo.getRikishi();
        if (rikishi != null) {
            // PRIMERO la grilla (no depende de BD)
            int slot = contadorConectados;
            contadorConectados++;

            controlVista.registrarLuchadorConectado(
                    rikishi.getNombre(), rikishi.getPeso(), slot);

            // DESPUES la BD (puede fallar)
            boolean guardado = controlRikishi.guardar(rikishi);
            controlVista.mostrar("Luchador " + (guardado ? "guardado" : "registrado")
                    + " en BD: " + rikishi.getNombre()
                    + (guardado ? "" : " (error BD - combate continua en memoria)"));
        }
    }

    public synchronized void hiloTerminado(HiloLuchador hilo) {
        // Monitoreo
    }
}
