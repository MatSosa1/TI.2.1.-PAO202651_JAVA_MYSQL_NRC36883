package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.Formatos;
import ec.edu.monster.modelo.PaisServicio;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.BusquedaVista;
import ec.edu.monster.vista.Vistas.BusquedaVista.Criterios;
import java.util.List;

/** ECUD5 Buscar Países y ECUD6 Filtrar (los criterios se envían juntos en la misma petición). */
public class BusquedaControlador extends ControladorConSesion {

    private final BusquedaVista vista;
    private final PaisServicio servicio;

    public BusquedaControlador(BusquedaVista vista, PaisServicio servicio, Sesion sesion, Runnable alSesionNoValida) {
        super(sesion, alSesionNoValida);
        this.vista = vista;
        this.servicio = servicio;
        vista.alBuscar(this::buscar);
    }

    private void buscar(Criterios criterios) {
        if (largo(criterios.nombre()) || largo(criterios.capital())) {
            vista.mostrarError(Formatos.NOMBRE_MUY_LARGO);
            return;
        }
        ejecutar(() -> {
            Resultado<ResultadoBusqueda> resultado = servicio.buscar(sesion.getToken(), criterios.nombre(),
                    criterios.capital(), Formatos.region(criterios.region()));
            ResultadoBusqueda datos = resultado.datos();
            vista.mostrarResultado(datos == null || datos.paises() == null ? List.of() : datos.paises(),
                    resultado.mensaje());
        }, vista::mostrarError);
    }

    private static boolean largo(String texto) {
        return texto != null && texto.trim().length() > Formatos.MAX_NOMBRE;
    }
}
