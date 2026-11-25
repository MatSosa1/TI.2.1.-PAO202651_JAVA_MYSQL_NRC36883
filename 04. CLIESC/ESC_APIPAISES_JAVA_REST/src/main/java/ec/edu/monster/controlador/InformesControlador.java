package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Datos.Informe;
import ec.edu.monster.modelo.ReporteServicio;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.InformesVista;

/** ECUD7 Generar Informes Estadísticos. */
public class InformesControlador extends ControladorConSesion {

    /** Mensaje del servidor sin países guardados (ECUD7, alterno 2). */
    static final String SIN_PAISES = "No hay países registrados para generar informes";
    static final String SUGERENCIA = ". Use «Consultar en la API» para guardar países.";

    private final InformesVista vista;
    private final ReporteServicio servicio;

    public InformesControlador(InformesVista vista, ReporteServicio servicio, Sesion sesion, Runnable alSesionNoValida) {
        super(sesion, alSesionNoValida);
        this.vista = vista;
        this.servicio = servicio;
        vista.alGenerar(this::generar);
    }

    private void generar() {
        ejecutar(() -> {
            Resultado<Informe> resultado = servicio.generar(sesion.getToken());
            vista.mostrarInforme(resultado.datos(), resultado.mensaje());
        }, mensaje -> vista.mostrarError(SIN_PAISES.equals(mensaje) ? mensaje + SUGERENCIA : mensaje));
    }
}
