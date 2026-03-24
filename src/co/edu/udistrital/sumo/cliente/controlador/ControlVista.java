//Pase el código del anterior proyecto a este clase que controla la vista (falta arreglarlo)
package co.edu.udistrital.sumo.cliente.controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author User
 */
public class ControlVista {
    package co.edu.udistrital.sumo.controlador.cliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acción desencadenada al presionar "Cargar Kimarites" en la vista del cliente.
 *
 * Propósito: Desacoplar el evento del botón de la lógica de negocio,
 * delegando completamente al {@link ControladorCliente}.
 * Cumple la separación evento/listener/performed exigida por el taller.
 * Se comunica con: {@link ControladorCliente} (único receptor de la acción).
 * Principio SOLID:
 * S — única responsabilidad: delegar la carga de kimarites al controlador.
 *
 * @author Grupo Programacióna avanzada 
 * @version 2.6
 * @see ControladorCliente
 * @see AccionConectar
 */
public class AccionCargarKimarites implements ActionListener {

    //Controlador del cliente al que se delega la acción
    private final ControladorCliente controlador;

    /**
     * Construye la acción con referencia al controlador del cliente.
     *
     * @param controlador controlador que gestionará la carga del archivo
     */
    public AccionCargarKimarites(ControladorCliente controlador) {
        this.controlador = controlador;
    }

    /**
     * Invocado por Swing cuando el usuario presiona el botón.
     * Delega al controlador para obtener la ruta del archivo y cargar los kimarites.
     *
     * @param e evento de acción generado por el botón
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.cargarKimarites();
    }
}


package co.edu.udistrital.sumo.controlador.cliente;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Acción desencadenada al presionar "Conectar al Servidor" en la vista del cliente.
 *
 * Propósito: Desacoplar el evento del botón de la lógica de conexión,
 * delegando completamente al {@link ControladorCliente}.
 * Cumple la separación evento/listener/performed exigida por el taller.
 * Se comunica con: {@link ControladorCliente} (único receptor de la acción).
 * Principio SOLID:
 * S — única responsabilidad: delegar la conexión al servidor al controlador.
 *
 * @author Grupo Programación avanzada
 * @version 2.2
 * @see ControladorCliente
 * @see AccionCargarKimarites
 */
public class AccionConectar implements ActionListener {

    //Controlador del cliente al que se delega la acción
    private final ControladorCliente controlador;

    /**
     * Construye la acción con referencia al controlador del cliente.
     *
     * @param controlador controlador que ejecutará la lógica de conexión
     */
    public AccionConectar(ControladorCliente controlador) {
        this.controlador = controlador;
    }

    /**
     * Invocado por Swing cuando el usuario presiona el botón.
     * Delega al controlador para validar datos e iniciar la conexión al servidor.
     *
     * @param e evento de acción generado por el botón
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        controlador.conectarAlServidor();
    }
}

}
