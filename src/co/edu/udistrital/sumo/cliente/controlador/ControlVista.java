package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.vista.VistaCliente;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Controlador de la vista del cliente (MVC - Controlador de vista).
 * 
 * Implementa ActionListener para manejar los eventos de los botones
 * de la VistaCliente. Diferencia los eventos por fuente (e.getSource())
 * cumpliendo con la separacion evento/listener/performed del enunciado.
 * 
 * Responsabilidades:
 *   - Registrar this como listener de btnCargar y btnConectar
 *   - Delegar acciones al ControlPrincipalC (no tiene logica de negocio)
 *   - Servir de intermediario entre ControlPrincipalC y VistaCliente
 * 
 * Principio SRP: solo gestiona eventos de UI, no hace logica ni red.
 * 
 * @author Grupo Programacion Avanzada
 */
public class ControlVista implements ActionListener {

    private final ControlPrincipalC cp;    // Controlador principal (logica)
    private final VistaCliente      vista; // Vista (presentacion)

    /**
     * Crea la vista, registra los listeners y la muestra.
     * 
     * @param cp controlador principal que recibe las acciones
     */
    public ControlVista(ControlPrincipalC cp) {
        this.cp    = cp;
        this.vista = new VistaCliente();
        // Registrar ESTA clase como listener de ambos botones
        vista.getBtnCargar().addActionListener(this);
        vista.getBtnConectar().addActionListener(this);
        vista.setVisible(true);  // Mostrar la ventana del cliente
    }

    /**
     * Maneja los eventos de los botones.
     * Diferencia por fuente (e.getSource()) y delega al controlador.
     * 
     * Boton "Cargar Kimarites":
     *   - Abre JFileChooser para seleccionar kimarites.properties
     *   - Delega a cp.cargarKimarites(ruta)
     * 
     * Boton "ENTRAR AL DOHYO":
     *   - Extrae nombre, peso y kimarites seleccionados de la vista
     *   - Delega a cp.conectar(nombre, peso, kimarites)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnCargar()) {
            // Boton "Cargar Kimarites" -> abrir JFileChooser
            String ruta = vista.seleccionarProperties();
            if (ruta != null) {
                cp.cargarKimarites(ruta);
            }
        } else if (e.getSource() == vista.getBtnConectar()) {
            // Boton "ENTRAR AL DOHYO" -> validar y conectar
            String nombre = vista.getNombre();
            double peso   = 0;
            try {
                peso = Double.parseDouble(vista.getPeso().trim());
            } catch (NumberFormatException ex) {
                mostrarMensaje("El peso debe ser un numero valido.");
                return;
            }
            List<String> seleccionados = vista.getKimaritesSeleccionados();
            cp.conectar(nombre, peso, seleccionados);
        }
    }

    // === Metodos de delegacion hacia la vista ===

    public void mostrarKimarites(List<String> kimarites) { vista.cargarKimarites(kimarites); }
    public void mostrarMensaje(String msg)               { vista.mostrarMensaje(msg); }
    public void mostrarEstado(String msg)                { vista.mostrarEstado(msg); }
    public void habilitarConectar(boolean habilitar)     { vista.setBtnConectarHabilitado(habilitar); }
    public void mostrarResultado(String resultado)       { vista.mostrarResultado(resultado); }
}
