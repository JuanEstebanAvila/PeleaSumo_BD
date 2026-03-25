package co.edu.udistrital.sumo.cliente.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Accion despues de presionar "Cargar Kimarites" en la vista del cliente.
 * Desacopla el evento del botón de la lógica de negocio,
 * delegando completamente al ControlPrincipalC.
 */
public class CargarKimarites implements ActionListener {

    private final ControlPrincipalC controlador;

    /**
     * @param controlador controlador que gestionará la carga del archivo
     */
    public CargarKimarites(ControlPrincipalC controlador) {
        this.controlador = controlador;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.cargarKimarites();
    }
}