package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.vista.VistaCliente;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Gestiona la VistaCliente y registra los eventos de los botones.
 * Implementa ActionListener y diferencia eventos por fuente (e.getSource()),
 * cumpliendo la separacion evento/listener/performed del enunciado.
 * Delega toda la logica al ControlPrincipalC.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlVista implements ActionListener {

    private final ControlPrincipalC cp;
    private final VistaCliente      vista;

    /**
     * Crea la vista, registra esta clase como listener y la muestra.
     * @param cp controlador principal del cliente
     */
    public ControlVista(ControlPrincipalC cp) {
        this.cp    = cp;
        this.vista = new VistaCliente();
        vista.getBtnCargar().addActionListener(this);
        vista.getBtnConectar().addActionListener(this);
        vista.setVisible(true);
    }

    /**
     * Maneja los eventos de los botones.
     * Diferencia por fuente y delega al controlador.
     * @param e evento generado por un boton
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnCargar()) {
            // La vista gestiona el JFileChooser y retorna la ruta
            String ruta = vista.seleccionarProperties();
            if (ruta != null) {
                cp.cargarKimarites(ruta);
            }
        } else if (e.getSource() == vista.getBtnConectar()) {
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

    /**
     * Carga los kimarites en la lista de la vista.
     * @param kimarites lista de tecnicas
     */
    public void mostrarKimarites(List<String> kimarites) {
        vista.cargarKimarites(kimarites);
    }

    /**
     * Muestra un mensaje informativo al usuario.
     * @param msg mensaje a mostrar
     */
    public void mostrarMensaje(String msg) {
        vista.mostrarMensaje(msg);
    }

    /**
     * Actualiza el texto de estado en la barra inferior.
     * @param msg texto de estado
     */
    public void mostrarEstado(String msg) {
        vista.mostrarEstado(msg);
    }

    /**
     * Habilita o deshabilita el boton de conectar.
     * @param habilitar true para habilitar
     */
    public void habilitarConectar(boolean habilitar) {
        vista.setBtnConectarHabilitado(habilitar);
    }

    /**
     * Muestra el resultado del combate y cierra la ventana.
     * @param gano true si el luchador gano
     */
    public void mostrarResultado(boolean gano) {
        vista.mostrarResultado(gano);
    }
}
