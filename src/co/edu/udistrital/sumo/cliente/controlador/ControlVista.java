package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.vista.VistaCliente;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Gestiona la VistaCliente y registra los eventos de los botones.
 * Implementa ActionListener y diferencia eventos por fuente (e.getSource()).
 * Delega toda la logica al ControlPrincipalC.
 *
 * @author Grupo Programacion Avanzada
 */
public class ControlVista implements ActionListener {

    private final ControlPrincipalC cp;
    private final VistaCliente      vista;

    public ControlVista(ControlPrincipalC cp) {
        this.cp    = cp;
        this.vista = new VistaCliente();
        vista.getBtnCargar().addActionListener(this);
        vista.getBtnConectar().addActionListener(this);
        vista.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtnCargar()) {
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

    public void mostrarKimarites(List<String> kimarites) {
        vista.cargarKimarites(kimarites);
    }

    public void mostrarMensaje(String msg) {
        vista.mostrarMensaje(msg);
    }

    public void mostrarEstado(String msg) {
        vista.mostrarEstado(msg);
    }

    public void habilitarConectar(boolean habilitar) {
        vista.setBtnConectarHabilitado(habilitar);
    }

    /**
     * Muestra el resultado del combate y cierra la ventana.
     * @param resultado "GANASTE", "PERDISTE" o "SIN_COMBATE"
     */
    public void mostrarResultado(String resultado) {
        vista.mostrarResultado(resultado);
    }
}
