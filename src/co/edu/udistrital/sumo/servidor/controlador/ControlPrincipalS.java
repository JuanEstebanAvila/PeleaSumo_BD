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
 * Controlador principal del servidor (MVC - Controlador).
 * 
 * Es el "cerebro" del servidor. Coordina TODO el flujo del torneo:
 * aceptacion de clientes, registro en BD, seleccion aleatoria de
 * combatientes, ejecucion de combates, persistencia en RAF y
 * notificacion de resultados a los clientes.
 * 
 * ====================== FLUJO COMPLETO DEL TORNEO ======================
 * 
 * FASE 1 - REGISTRO (Constructor + ejecutar):
 *   1. JFileChooser selecciona CredencialesSer.properties
 *   2. Se cargan: PUERTO (socket), BD.URL, BD.USER, BD.PASSWORD
 *   3. Se crea la VistaServidor con la grilla de 6 slots
 *   4. Se abre ServerSocket en el PUERTO configurado
 *   5. Se aceptan exactamente 6 conexiones de clientes
 *   6. Cada cliente genera un HiloLuchador que:
 *      a. Lee "nombre|peso|k1,k2,..." del socket
 *      b. Crea un objeto Rikishi
 *      c. Llama registrarLuchador() -> actualiza grilla + guarda en BD
 *      d. Se queda bloqueado en esperarOrden() esperando instrucciones
 * 
 * FASE 2 - COMBATES (ejecutarCombate):
 *   1. Se mezclan los 6 luchadores aleatoriamente (Collections.shuffle)
 *   2. Se ejecutan TOTAL_LUCHADORES-1 combates (5 con 6 luchadores):
 *      - Combate 1: Luchador A vs Luchador B
 *      - Combate 2: Ganador(1) vs Luchador C
 *      - Combate 3: Ganador(2) vs Luchador D
 *      - Combate 4: Ganador(3) vs Luchador E
 *      - Combate 5: Ganador(4) vs Luchador F
 *   3. Cada combate:
 *      a. Se crea un Dohyo nuevo (monitor de sincronizacion)
 *      b. Se asigna el Dohyo a los dos HiloLuchador via asignarCombate()
 *      c. Los hilos suben al Dohyo, se sincronizan, y combaten por turnos
 *      d. El Dohyo determina el ganador (kimarite aleatorio con probabilidad)
 *      e. El PERDEDOR recibe "PERDISTE" de inmediato -> su cliente cierra
 *      f. El GANADOR se queda bloqueado en esperarOrden() -> listo para otro combate
 *      g. Se actualiza victorias en la BD
 *      h. Se guardan datos en el RAF (obtenidos de la BD + campo G/P del servidor)
 * 
 * FASE 3 - CIERRE:
 *   1. El campeon final recibe "GANASTE"
 *   2. Los luchadores no seleccionados reciben "SIN_COMBATE"
 *   3. Se espera que TODOS los clientes envien "LISTO"
 *   4. Se lee el RAF y se muestra en consola (System.out.println)
 *   5. Se cierra la VistaServidor y termina el proceso
 * 
 * ====================== PATRONES APLICADOS ======================
 *   - MVC: este es el Controlador, VistaServidor es la Vista, Rikishi el Modelo
 *   - Observer: Dohyo notifica a ControlVistaS via ICombateObservador
 *   - Singleton: ConexionBD.getInstancia() para la BD
 *   - DAO: RikishiDAO encapsula las consultas SQL
 *   - Monitor: Dohyo usa synchronized + wait/notify para sincronizar hilos
 * 
 * @author Grupo Programacion Avanzada
 */
public class ControlPrincipalS {

    /** Numero exacto de luchadores que deben conectarse antes de iniciar */
    private static final int TOTAL_LUCHADORES = 6;
    
    /** 
     * Numero de combates: TOTAL-1 para agotar todos los pendientes.
     * Con 6 luchadores: 5 combates (carry-over del ganador).
     * El enunciado dice: "continuara asi hasta terminar con los luchadores
     * registrados en la base de datos".
     */
    private static final int NUM_COMBATES = TOTAL_LUCHADORES - 1;

    private final ControlVistaS    controlVista;     // Controlador de la vista del servidor
    private final ControlRikishi   controlRikishi;   // Controlador de acceso a la BD
    private final ConexionServidor cnxServidor;       // Wrapper del ServerSocket

    /** Lista de TODOS los hilos de clientes conectados (para buscar por nombre) */
    private final List<HiloLuchador> hilosConectados = new ArrayList<>();
    
    /** Lista de luchadores PENDIENTES de combatir (se van removiendo) */
    private final List<Rikishi>      pendientes      = new ArrayList<>();

    /** Contador thread-safe de luchadores conectados (para asignar slot en grilla) */
    private volatile int contadorConectados = 0;

    /**
     * Constructor: carga configuracion, crea vista, abre socket e inicia el servidor.
     * Si el usuario cancela el JFileChooser, se termina la aplicacion.
     */
    public ControlPrincipalS() {
        // Paso 1: Seleccionar archivo .properties del servidor
        String rutaProperties = seleccionarProperties();
        if (rutaProperties == null) {
            System.exit(0);
            this.controlVista   = null;
            this.controlRikishi = null;
            this.cnxServidor    = null;
            return;
        }

        // Paso 2: Cargar PUERTO + credenciales BD desde el properties
        int puerto = ConexionProperties.cargar(rutaProperties);
        
        // Paso 3: Crear controladores
        this.controlVista   = new ControlVistaS();     // Crea y muestra VistaServidor
        this.controlRikishi = new ControlRikishi();     // Crea RikishiDAO
        this.cnxServidor    = new ConexionServidor(puerto); // Wrapper del ServerSocket

        // Paso 4: Iniciar hilo principal del servidor
        iniciar();
    }

    /** Abre JFileChooser para seleccionar CredencialesSer.properties */
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

    /** Lanza el hilo principal del servidor (no bloquea el EDT de Swing) */
    private void iniciar() {
        Thread hiloServidor = new Thread(this::ejecutar, "HiloServidorPrincipal");
        hiloServidor.setDaemon(false);  // No es daemon: mantiene viva la JVM
        hiloServidor.start();
    }

    /**
     * Logica principal del servidor. Se ejecuta en un hilo separado.
     * Contiene las 3 fases: registro, combates y cierre.
     */
    private void ejecutar() {
        try {
            // ========== FASE 1: REGISTRO DE LUCHADORES ==========
            cnxServidor.iniciar();  // Abrir ServerSocket
            controlVista.mostrar("Servidor iniciado. Esperando luchadores...");
            controlVista.actualizarEstado(
                    "Esperando " + TOTAL_LUCHADORES + " luchadores...");

            // Aceptar exactamente 6 conexiones TCP
            while (contadorConectados < TOTAL_LUCHADORES) {
                Socket socket = cnxServidor.aceptarConexion();  // Bloquea hasta que llega uno
                controlVista.mostrar("Cliente conectado: "
                        + socket.getInetAddress().getHostAddress());

                // Crear un hilo dedicado para este cliente (requisito: un hilo por cliente)
                HiloLuchador hilo = new HiloLuchador(socket, this);
                hilosConectados.add(hilo);
                hilo.start();  // El hilo lee datos, crea Rikishi y llama registrarLuchador()

                Thread.sleep(1000);  // Dar tiempo a que el hilo procese
            }

            cnxServidor.cerrar();  // Ya no se aceptan mas conexiones
            controlVista.mostrar(
                    TOTAL_LUCHADORES + " luchadores conectados. Iniciando torneo...");
            controlVista.actualizarEstado("Todos conectados. Preparando combates...");

            Thread.sleep(2000);

            // Limpiar archivo RAF de torneos anteriores
            ConexionAleatoria.limpiar();

            // Preparar lista de pendientes EN MEMORIA (no depende de la BD)
            synchronized (this) {
                pendientes.clear();
                for (HiloLuchador h : hilosConectados) {
                    if (h.getRikishi() != null) {
                        pendientes.add(h.getRikishi());
                    }
                }
            }
            // Seleccion ALEATORIA (requisito del enunciado)
            Collections.shuffle(pendientes);

            controlVista.mostrar("Luchadores en el torneo: " + pendientes.size());

            // ========== FASE 2: COMBATES ==========
            HiloLuchador hiloGanadorActual = null;  // Hilo del ganador actual (se reutiliza)
            String nombreGanadorActual     = null;

            for (int numCombate = 1; numCombate <= NUM_COMBATES; numCombate++) {
                Object[] resultado = ejecutarCombate(
                        numCombate, nombreGanadorActual, hiloGanadorActual);
                if (resultado == null) break;  // Error critico

                nombreGanadorActual = (String) resultado[0];
                hiloGanadorActual   = (HiloLuchador) resultado[1];

                if (numCombate < NUM_COMBATES) {
                    Thread.sleep(3000);  // Pausa visual entre combates
                    // El ganador esta bloqueado en esperarOrden() dentro de su hilo
                    // asignarCombate() lo despertara en el siguiente ciclo
                }
            }

            // ========== FASE 3: CIERRE ==========
            
            // El campeon recibe "GANASTE" — su cliente puede cerrar
            if (hiloGanadorActual != null) {
                controlVista.mostrar("=== CAMPEON DEL TORNEO: "
                        + nombreGanadorActual + " ===");
                hiloGanadorActual.enviarResultadoFinal("GANASTE");
            }

            // Luchadores que nunca combatieron reciben "SIN_COMBATE"
            notificarSinCombate();

            // Esperar que TODOS los clientes envien "LISTO" (protocolo)
            controlVista.mostrar("Esperando confirmacion de todos los clientes...");
            esperarTodosListo();

        } catch (IOException e) {
            controlVista.mostrar("Error del servidor: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // SIEMPRE mostrar RAF en consola (incluso si hubo errores)
            mostrarRAFEnConsola();
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            controlVista.cerrar();
        }
    }

    /**
     * Ejecuta un combate entre dos luchadores.
     * 
     * Retorna Object[]{nombreGanador, hiloGanador} o null si hay error.
     * 
     * El PERDEDOR recibe "PERDISTE" de inmediato.
     * El GANADOR queda bloqueado en esperarOrden() — NO recibe resultado aun.
     * En el siguiente combate, asignarCombate() lo despierta directamente.
     */
    private Object[] ejecutarCombate(int numCombate, String nombreGanadorAnt,
                                     HiloLuchador hiloGanadorAnt)
            throws IOException, InterruptedException {

        controlVista.mostrar("=== Preparando Combate " + numCombate + " ===");
        controlVista.resetearSeleccion();

        Rikishi luchador1, luchador2;
        HiloLuchador hilo1, hilo2;

        if (nombreGanadorAnt != null && hiloGanadorAnt != null) {
            // CARRY-OVER: el ganador anterior enfrenta al siguiente pendiente
            luchador1 = hiloGanadorAnt.getRikishi();
            hilo1     = hiloGanadorAnt;
            if (pendientes.isEmpty()) {
                controlVista.mostrar("No hay mas pendientes.");
                return new Object[]{ nombreGanadorAnt, hiloGanadorAnt };
            }
            luchador2 = pendientes.remove(0);  // Siguiente pendiente (aleatorio)
            hilo2     = buscarHilo(luchador2.getNombre());
        } else {
            // PRIMER COMBATE: sacar dos del inicio de la lista (ya mezclada)
            if (pendientes.size() < 2) {
                controlVista.mostrar("No hay suficientes luchadores.");
                return null;
            }
            luchador1 = pendientes.remove(0);
            luchador2 = pendientes.remove(0);
            hilo1     = buscarHilo(luchador1.getNombre());
            hilo2     = buscarHilo(luchador2.getNombre());
        }

        if (hilo1 == null || hilo2 == null) {
            controlVista.mostrar("Error: no se encontraron los hilos.");
            return null;
        }

        // Mostrar seleccion visual en la grilla (resalta en naranja)
        controlVista.mostrarSeleccionCombatientes(
                luchador1.getNombre(), luchador1.getPeso(),
                luchador2.getNombre(), luchador2.getPeso());

        Thread.sleep(2000);  // Pausa para que el usuario vea la seleccion

        controlVista.mostrar("Combate " + numCombate + ": "
                + luchador1.getNombre() + " vs " + luchador2.getNombre());

        // Actualizar panel de combate (nombres en azul y rojo)
        controlVista.mostrarLuchadorEnDohyo(
                luchador1.getNombre(), luchador1.getPeso(), 0);
        controlVista.mostrarLuchadorEnDohyo(
                luchador2.getNombre(), luchador2.getPeso(), 1);
        controlVista.mostrarInicioCombate(
                luchador1.getNombre(), luchador2.getNombre());

        // Crear un NUEVO Dohyo para este combate (uno por combate)
        Dohyo dohyo = new Dohyo();
        dohyo.agregarObservador(controlVista);  // Observer: notifica kimarites a la vista

        // CountDownLatch(2): esperar que AMBOS hilos terminen de combatir
        CountDownLatch latchCombate = new CountDownLatch(2);
        
        // Despertar a los dos hilos: asignarCombate() envia orden COMBATIR
        hilo1.asignarCombate(dohyo, 0, latchCombate);
        hilo2.asignarCombate(dohyo, 1, latchCombate);

        // Bloquear hasta que ambos terminen (el Dohyo decidio un ganador)
        latchCombate.await();

        // Determinar ganador (el Dohyo ya lo tiene calculado)
        Rikishi ganador = dohyo.getGanador();
        if (ganador == null) {
            controlVista.mostrar("Error: combate sin ganador.");
            return null;
        }

        boolean ganoEl1 = ganador.getNombre().equals(luchador1.getNombre());
        Rikishi perdedor          = ganoEl1 ? luchador2 : luchador1;
        HiloLuchador hiloPerdedor = ganoEl1 ? hilo2     : hilo1;
        HiloLuchador hiloGanador  = ganoEl1 ? hilo1     : hilo2;

        // Actualizar victorias del ganador en la BD (requisito: sumar 1 a victorias)
        controlRikishi.actualizarVictorias(
                ganador.getNombre(), ganador.getVictorias());

        // Actualizar vista: panel dorado con ganador, grilla con perdedor
        controlVista.mostrarGanador(ganador.getNombre(), ganador.getVictorias());
        controlVista.marcarLuchadorParticipo(perdedor.getNombre());

        // Guardar en RAF: datos de la BD + campo G/P que agrega el servidor
        guardarEnRAF(ganador, perdedor, numCombate);

        // PERDEDOR recibe "PERDISTE" de inmediato -> su cliente puede cerrar
        hiloPerdedor.enviarResultadoFinal("PERDISTE");

        // GANADOR: su hilo ahora esta en esperarOrden() esperando
        // asignarCombate() lo despertara en el siguiente ciclo

        controlVista.mostrar("Combate " + numCombate + " finalizado. Ganador: "
                + ganador.getNombre());

        return new Object[]{ ganador.getNombre(), hiloGanador };
    }

    /**
     * Guarda los resultados de un combate en el archivo de acceso aleatorio.
     * 
     * REQUISITO DEL ENUNCIADO: "los datos que se envian al archivo deben venir
     * expresamente de la base de datos, a traves de una consulta".
     * El campo G/P (Gano/Perdio) lo agrega el servidor.
     * 
     * Si la BD no esta disponible, usa los datos en memoria como respaldo.
     */
    private void guardarEnRAF(Rikishi ganador, Rikishi perdedor, int numCombate) {
        // Consultar datos DESDE la BD (requisito)
        Rikishi ganadorBD  = controlRikishi.consultarPorNombre(ganador.getNombre());
        Rikishi perdedorBD = controlRikishi.consultarPorNombre(perdedor.getNombre());
        Rikishi gFinal = ganadorBD  != null ? ganadorBD  : ganador;   // Respaldo en memoria
        Rikishi pFinal = perdedorBD != null ? perdedorBD : perdedor;

        try {
            // Guardar ganador con campo 'G' (lo agrega el servidor, no la BD)
            ConexionAleatoria.guardarResultado(
                    gFinal.getNombre(), gFinal.getPeso(),
                    ganador.getVictorias(), true, numCombate);
            // Guardar perdedor con campo 'P'
            ConexionAleatoria.guardarResultado(
                    pFinal.getNombre(), pFinal.getPeso(),
                    perdedor.getVictorias(), false, numCombate);
            controlVista.mostrar("Combate " + numCombate
                    + " guardado en archivo de acceso aleatorio.");
        } catch (IOException e) {
            controlVista.mostrar("Error al guardar en RAF: " + e.getMessage());
        }
    }

    /** Envia "SIN_COMBATE" a hilos que nunca fueron seleccionados para combatir */
    private void notificarSinCombate() {
        for (HiloLuchador hilo : hilosConectados) {
            if (hilo.isAlive() && !hilo.fueCombatiente()) {
                String nombre = hilo.getRikishi() != null
                        ? hilo.getRikishi().getNombre() : "?";
                controlVista.mostrar("Notificando SIN_COMBATE a: " + nombre);
                hilo.notificarSinCombate(null);
            }
        }
    }

    /** Espera que TODOS los hilos terminen (clientes enviaron LISTO) */
    private void esperarTodosListo() {
        for (HiloLuchador hilo : hilosConectados) {
            try { hilo.join(10000); }  // Max 10 segundos por cliente
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        controlVista.mostrar("Todos los clientes han confirmado.");
    }

    /** Lee el RAF y lo muestra en consola y en el log de la vista */
    private void mostrarRAFEnConsola() {
        try {
            String contenidoRAF = ConexionAleatoria.leerTodos();
            if (contenidoRAF != null && !contenidoRAF.isEmpty()) {
                System.out.println();
                System.out.println("=== RESULTADOS DEL TORNEO (Archivo de Acceso Aleatorio) ===");
                System.out.println(contenidoRAF);
                System.out.println("============================================================");
                controlVista.mostrar("=== Contenido del archivo de acceso aleatorio ===");
                controlVista.mostrar(contenidoRAF);
            } else {
                System.out.println("El archivo de acceso aleatorio esta vacio.");
                controlVista.mostrar("El archivo de acceso aleatorio esta vacio.");
            }
        } catch (IOException e) {
            System.out.println("Error al leer RAF: " + e.getMessage());
            controlVista.mostrar("Error al leer RAF: " + e.getMessage());
        }
    }

    /** Busca el HiloLuchador correspondiente a un nombre */
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
     * Llamado por HiloLuchador cuando recibe datos del cliente.
     * 
     * synchronized: evita que dos hilos modifiquen contadorConectados a la vez.
     * 
     * IMPORTANTE: actualiza la grilla PRIMERO, luego intenta guardar en BD.
     * Asi la grilla funciona aunque la BD este caida.
     * Si la BD falla, muestra el error exacto en el log.
     */
    public synchronized void registrarLuchador(HiloLuchador hilo) {
        Rikishi rikishi = hilo.getRikishi();
        if (rikishi != null) {
            // PRIMERO: actualizar grilla visual (no depende de BD)
            int slot = contadorConectados;
            contadorConectados++;
            controlVista.registrarLuchadorConectado(
                    rikishi.getNombre(), rikishi.getPeso(), slot);

            // DESPUES: intentar guardar en BD (puede fallar)
            boolean guardado = controlRikishi.guardar(rikishi);
            if (guardado) {
                controlVista.mostrar("[BD] " + rikishi.getNombre()
                        + " guardado en la base de datos.");
            } else {
                String error = controlRikishi.getUltimoError();
                controlVista.mostrar("[BD ERROR] " + rikishi.getNombre()
                        + " NO se guardo. Razon: " + error);
            }
        }
    }

    /** Callback de monitoreo cuando un hilo termina */
    public synchronized void hiloTerminado(HiloLuchador hilo) {}
}
