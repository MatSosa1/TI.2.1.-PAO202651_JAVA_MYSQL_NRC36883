package ec.edu.monster.controlador;

import ec.edu.monster.modelo.EstadoServidor;
import ec.edu.monster.vista.Vistas.EstadoVista;

/** Mantiene actualizado el indicador «Servicio disponible / no disponible». */
public class EstadoControlador {

    private final EstadoVista vista;
    private final EstadoServidor estado;

    public EstadoControlador(EstadoVista vista, EstadoServidor estado) {
        this.vista = vista;
        this.estado = estado;
        vista.alRevisarEstado(this::revisar);
    }

    private void revisar() {
        vista.mostrarEstado(estado.disponible());
    }
}
