package co.edu.udistrital.sumo.servidor.controlador;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * Punto de entrada del servidor.
 * Solo crea el ControlPrincipalS con la ruta del properties.
 * No crea objetos del modelo, no maneja vistas directamente.
 *
 * @author Grupo Programacion Avanzada
 */
public class LauncherServidor {

    public static void main(String[] args) {
        // Seleccionar el properties del servidor via JFileChooser
        JFileChooser fc = new JFileChooser(new File("Data/Servidor/"));
        fc.setFileFilter(new FileNameExtensionFilter("Archivo de propiedades", "properties"));
        fc.setDialogTitle("Seleccione el properties del servidor");
        int resultado = fc.showOpenDialog(null);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            String ruta = fc.getSelectedFile().getAbsolutePath();
            ControlPrincipalS cp = new ControlPrincipalS(ruta);
            cp.iniciar();
        }
    }
}
