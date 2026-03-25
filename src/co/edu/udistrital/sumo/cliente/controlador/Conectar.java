package co.edu.udistrital.sumo.cliente.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Accion desencadenada al presionar "Conectar al Servidor" en la vista del cliente.
 * Desacopla el evento del boton de la logica de conexion,
 * delegando completamente al ControlPrincipalC.
 */
public class Conectar implements ActionListener {

    private final ControlPrincipalC controlador;

    /**
     * @param controlador controlador que ejecutará la lógica de conexión
     */
    public Conectar(ControlPrincipalC controlador) {
        this.controlador = controlador;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.conectarAlServidor();
    }
}