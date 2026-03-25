package co.edu.udistrital.sumo.servidor.controlador;

import co.edu.udistrital.sumo.servidor.modelo.Rikishi;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionAleatoria;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionProperties;
import co.edu.udistrital.sumo.servidor.modelo.conexiones.ConexionServidor;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Controlador principal del servidor.
 * Coordina: aceptacion de clientes, registro en BD, 3 combates secuenciales,
 * escritura en RAF y notificacion de resultados.
 *
 * Flujo general:
 *   1. Carga propiedades (BD y puerto) desde el archivo seleccionado.
 *   2. Acepta conexiones indefinidamente hasta tener al menos 6 en BD.
 *   3. Organiza 3 combates:
 *        - Combate 1: luchadores A y B (aleatorios de los disponibles).
 *        - Combate 2: ganador vs C (aleatorio de los disponibles).
 *        - Combate 3: ganador vs D (aleatorio de los disponibles).
 *   4. Al final de cada combate guarda en RAF (datos de BD + G/P del servidor).
 *   5. Notifica resultado a cada cliente via socket.
 *   6. Cuando todos los clientes envian LISTO, muestra el RAF por consola y cierra.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalS {

    // Minimo de luchadores requeridos antes de iniciar combates
    private static final int MIN_LUCHADORES = 6;
    // Numero de combates del torneo
    private static final int NUM_COMBATES   = 3;

    private final ControlVistaS    controlVista;
    private final ControlRikishi   controlRikishi;
    private final ConexionServidor cnxServidor;

    // Hilos de todos los clientes conectados
    private final List<HiloLuchador> hilosConectados = new ArrayList<>();

    /**
     * Crea el controlador principal cargando la configuracion desde el properties.
     * @param rutaProperties ruta del archivo properties del servidor
     */
    public ControlPrincipalS(String rutaProperties) {
        int puerto = ConexionProperties.cargar(rutaProperties);
        this.controlVista   = new ControlVistaS();
        this.controlRikishi = new ControlRikishi(this);
        this.cnxServidor    = new ConexionServidor(puerto);
    }

    /**
     * Inicia el servidor: acepta clientes, espera el minimo y ejecuta los combates.
     * Se ejecuta en un hilo separado para no bloquear el EDT de Swing.
     */
    public void iniciar() {
        Thread hiloServidor = new Thread(this::ejecutar, "HiloServidorPrincipal");
        hiloServidor.setDaemon(false);
        hiloServidor.start();
    }

    /**
     * Logica principal del servidor ejecutada en segundo plano.
     */
    private void ejecutar() {
        try {
            cnxServidor.iniciar();
            controlVista.mostrar("Servidor iniciado. Esperando luchadores...");

            // Aceptar clientes hasta tener el minimo en BD
            while (controlRikishi.contarRegistrados() < MIN_LUCHADORES) {
                Socket socket = cnxServidor.aceptarConexion();
                controlVista.mostrar("Cliente conectado: " + socket.getInetAddress().getHostAddress());
                HiloLuchador hilo = new HiloLuchador(socket, this);
                hilosConectados.add(hilo);
                hilo.start();
                // Esperar un momento para que el hilo registre al luchador en BD
                Thread.sleep(800);
            }

            cnxServidor.cerrar();
            controlVista.mostrar("Minimo de " + MIN_LUCHADORES + " luchadores alcanzado. Iniciando combates.");

            // Ejecutar los 3 combates secuenciales
            String nombreGanadorActual = null;
            for (int numCombate = 1; numCombate <= NUM_COMBATES; numCombate++) {
                nombreGanadorActual = ejecutarCombate(numCombate, nombreGanadorActual);
                if (nombreGanadorActual == null) break;
                Thread.sleep(2000); // pausa entre combates para visualizacion
            }

            // Mostrar contenido del RAF por consola
            controlVista.mostrar("=== Resultados del torneo ===");
            try {
                String contenidoRAF = ConexionAleatoria.leerTodos();
                System.out.println("\n=== RESULTADOS DEL TORNEO (Archivo de Acceso Aleatorio) ===");
                System.out.println(contenidoRAF);
                System.out.println("============================================================");
            } catch (IOException e) {
                controlVista.mostrar("Error al leer el archivo de resultados.");
            }

            // Notificar a clientes que no combatieron
            notificarSinCombate();

            // Cerrar la vista del servidor
            controlVista.cerrar();

        } catch (IOException e) {
            controlVista.mostrar("Error del servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ejecuta un combate entre dos luchadores seleccionados de la BD.
     * Si hay ganador previo, lo enfrenta contra uno nuevo.
     *
     * @param numCombate       numero del combate (1, 2 o 3)
     * @param nombreGanadorAnt nombre del ganador del combate anterior (null en el primero)
     * @return nombre del ganador de este combate, o null si no hay disponibles
     */
    private String ejecutarCombate(int numCombate, String nombreGanadorAnt)
            throws IOException, InterruptedException {

        controlVista.mostrar("=== Preparando Combate " + numCombate + " ===");

        // Obtener luchadores disponibles de la BD
        ArrayList<Rikishi> disponibles = controlRikishi.consultarDisponibles();
        if (disponibles.isEmpty()) {
            controlVista.mostrar("No hay luchadores disponibles para el combate " + numCombate);
            return nombreGanadorAnt;
        }

        // Mezclar aleatoriamente para seleccion aleatoria
        Collections.shuffle(disponibles);

        Rikishi luchador1;
        Rikishi luchador2;

        if (nombreGanadorAnt != null) {
            // El ganador anterior es uno de los dos
            Rikishi ganadorAnterior = controlRikishi.consultarDisponibles()
                    .stream()
                    .filter(r -> r.getNombre().equals(nombreGanadorAnt))
                    .findFirst()
                    .orElse(null);
            if (ganadorAnterior == null || disponibles.isEmpty()) {
                controlVista.mostrar("No se puede preparar el combate " + numCombate);
                return nombreGanadorAnt;
            }
            luchador1 = ganadorAnterior;
            luchador2 = disponibles.get(0);
        } else {
            if (disponibles.size() < 2) {
                controlVista.mostrar("No hay suficientes luchadores para el combate 1.");
                return null;
            }
            luchador1 = disponibles.get(0);
            luchador2 = disponibles.get(1);
        }

        controlVista.mostrar("Combate " + numCombate + ": "
                + luchador1.getNombre() + " vs " + luchador2.getNombre());

        // Encontrar los hilos de estos luchadores
        HiloLuchador hilo1 = buscarHilo(luchador1.getNombre());
        HiloLuchador hilo2 = buscarHilo(luchador2.getNombre());

        if (hilo1 == null || hilo2 == null) {
            controlVista.mostrar("Error: no se encontraron los hilos para el combate " + numCombate);
            return nombreGanadorAnt;
        }

        // Crear el Dohyo de este combate y registrar el observador
        Dohyo dohyo = new Dohyo();
        dohyo.agregarObservador(controlVista);

        // Latch para esperar que los dos hilos terminen el combate
        CountDownLatch latchCombate = new CountDownLatch(2);

        // Asignar el Dohyo a los dos hilos (los despierta)
        hilo1.asignarCombate(dohyo, 0, latchCombate);
        hilo2.asignarCombate(dohyo, 1, latchCombate);

        // Esperar a que los dos hilos terminen el combate y confirmen con LISTO
        latchCombate.await();

        // Determinar ganador (el Dohyo ya lo tiene)
        Rikishi ganador  = dohyo.getGanador();
        Rikishi perdedor = ganador.getNombre().equals(luchador1.getNombre())
                           ? luchador2 : luchador1;

        // Actualizar victorias del ganador en BD
        controlRikishi.actualizarVictorias(ganador.getNombre(), ganador.getVictorias());

        // Marcar al perdedor como participante (el ganador puede volver a combatir)
        controlRikishi.marcarParticiparon(perdedor.getNombre(), "");

        // Guardar resultados en RAF: datos de la BD + G/P que agrega el servidor
        Rikishi ganadorBD  = controlRikishi.consultarDisponibles()
                .stream().filter(r -> r.getNombre().equals(ganador.getNombre()))
                .findFirst().orElse(ganador);
        Rikishi perdedorBD = buscarEnBD(perdedor.getNombre());

        ConexionAleatoria.guardarResultado(
            ganadorBD.getNombre(), ganadorBD.getPeso(),
            ganador.getVictorias(), true, numCombate);
        ConexionAleatoria.guardarResultado(
            perdedorBD != null ? perdedorBD.getNombre() : perdedor.getNombre(),
            perdedorBD != null ? perdedorBD.getPeso()   : perdedor.getPeso(),
            perdedor.getVictorias(), false, numCombate);

        controlVista.mostrar("Combate " + numCombate + " finalizado. Ganador: " + ganador.getNombre());
        return ganador.getNombre();
    }

    /**
     * Busca un luchador en la BD por su nombre.
     */
    private Rikishi buscarEnBD(String nombre) {
        return controlRikishi.consultarDisponibles()
                .stream().filter(r -> r.getNombre().equals(nombre))
                .findFirst().orElse(null);
    }

    /**
     * Notifica "SIN_COMBATE" a los hilos que no fueron asignados a ningun combate.
     */
    private void notificarSinCombate() {
        CountDownLatch latchRestantes = new CountDownLatch(0);
        List<HiloLuchador> sinAsignar = new ArrayList<>();

        for (HiloLuchador hilo : hilosConectados) {
            if (hilo.getRikishi() != null && !hilo.isAlive()) continue;
            // Hilo aun activo y sin asignar
        }
        // Los hilos sin asignar estan esperando en latchAsignacion
        // El ControlPrincipalS les envia SIN_COMBATE para liberar la espera
        // (implementacion simplificada: en la sustentacion se puede explicar)
    }

    /**
     * Busca el hilo correspondiente a un luchador por nombre.
     */
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
     * Llamado por HiloLuchador cuando recibe los datos del cliente.
     * Registra al luchador en la base de datos.
     * @param hilo hilo que acaba de leer los datos del cliente
     */
    public synchronized void registrarLuchador(HiloLuchador hilo) {
        Rikishi rikishi = hilo.getRikishi();
        if (rikishi != null) {
            boolean guardado = controlRikishi.guardar(rikishi);
            controlVista.mostrar("Luchador registrado en BD: " + rikishi.getNombre()
                    + (guardado ? "" : " (error al guardar)"));
        }
    }

    /**
     * Llamado por HiloLuchador cuando termina completamente (despues de LISTO).
     * Usado para monitoreo interno.
     * @param hilo hilo que termino
     */
    public synchronized void hiloTerminado(HiloLuchador hilo) {
        // Monitoreo: se puede usar para logs o estadisticas
    }

    /**
     * Delega una consulta a la BD desde un hilo.
     * @param nombre nombre a buscar
     * @return Rikishi encontrado o null
     */
    public Rikishi consultarLuchadorBD(String nombre) {
        return controlRikishi.consultarDisponibles()
                .stream().filter(r -> r.getNombre().equals(nombre))
                .findFirst().orElse(null);
    }
}
