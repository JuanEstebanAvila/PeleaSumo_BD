package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.cliente.vista.VistaCliente;

/**
 * Controlador de vista del cliente.
 *
 * Responsabilidad única: registrar los listeners desacoplados
 * en los botones de la vista y conectarlos con el ControlPrincipalC.
 *
 * De esta forma ControlPrincipalC no necesita conocer los detalles
 * de cómo están construidos los botones, y la VistaCliente no
 * conoce al controlador directamente.
 *
 * Principio SOLID — S: única responsabilidad: enlazar vista y controlador.
 *
 * @author Grupo Programación Avanzada
 * @version 1.0
 * @see VistaCliente
 * @see ControlPrincipalC
 * @see CargarKimarites
 * @see Conectar
 */
public class ControlVista {

    private final VistaCliente    vista;
    private final ControlPrincipalC controlador;

    /**
     * Construye el controlador de vista y registra inmediatamente
     * los listeners en los botones.
     *
     * @param vista       vista del cliente
     * @param controlador controlador principal del cliente
     */
    public ControlVista(VistaCliente vista, ControlPrincipalC controlador) {
        this.vista       = vista;
        this.controlador = controlador;
        registrarListeners();
    }

    /**
     * Asigna los listeners desacoplados a cada botón de la vista.
     * Este método SOLO registra — no contiene lógica de negocio.
     */
    private void registrarListeners() {
        vista.getBtnCargarKimarites().addActionListener(
            new CargarKimarites(controlador));
        vista.getBtnConectar().addActionListener(
            new Conectar(controlador));
    }
}