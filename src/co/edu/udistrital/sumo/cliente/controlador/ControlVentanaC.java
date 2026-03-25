/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package co.edu.udistrital.sumo.cliente.controlador;

import co.edu.udistrital.sumo.modelo.cliente.CargadorPropiedades;
import co.edu.udistrital.sumo.vista.cliente.VistaCliente;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingUtilities;

/**
 * Controlador que gestiona la interfaz gráfica del cliente.
 * Desacopla los eventos de la vista de la lógica de comunicación.
 * Cumple con el requisito de no usar verbos en nombres de clases.
 */
public class ControlVentanaC {

    private VistaCliente vista;
    private ControlCliente controlLogica;
    private CargadorPropiedades cargador;

    public ControlVentanaC(ControlCliente controlLogica) {
        this.vista = new VistaCliente();
        this.controlLogica = controlLogica;
        this.cargador = new CargadorPropiedades();
        configurarEventos();
        this.vista.setVisible(true);
    }

    /**
     * Configura los listeners de los botones sin usar clases externas con verbos.
     * Cumple con la separación de eventos, listener y performed.
     */
    private void configurarEventos() {
        // Listener para cargar técnicas
        vista.getBtnCargar().addActionListener(e -> {
            String ruta = vista.solicitarRutaArchivo();
            if (ruta != null) {
                try {
                    List<String> tecnicas = cargador.cargarKimarites(ruta);
                    vista.actualizarListaTecnicas(tecnicas);
                } catch (IOException ex) {
                    vista.mostrarError("Error cargando archivo: " + ex.getMessage());
                }
            }
        });

        // Listener para conectar y combatir
        vista.getBtnConectar().addActionListener(e -> iniciarCombate());
    }

    private void iniciarCombate() {
        // El cliente lanza un hilo para no bloquear la UI durante la red
        new Thread(() -> {
            try {
                actualizarEstado("Enviando luchador al Dohjo...");
                
                // Obtenemos solo los datos (String), no objetos del modelo (Rikishi)
                String datosLuchador = vista.getDatosFormulario();
                
                String resultado = controlLogica.procesarCombate(datosLuchador);
                
                boolean gano = "GANASTE!!!".equals(resultado);
                actualizarEstado(gano ? "¡VICTORIA!" : "Derrota...");
                
                SwingUtilities.invokeLater(() -> vista.mostrarResultadoCombate(gano));
                
            } catch (Exception ex) {
                actualizarEstado("Error: " + ex.getMessage());
            }
        }).start();
    }

    private void actualizarEstado(String msg) {
        SwingUtilities.invokeLater(() -> vista.mostrarEstado(msg));
    }
}
