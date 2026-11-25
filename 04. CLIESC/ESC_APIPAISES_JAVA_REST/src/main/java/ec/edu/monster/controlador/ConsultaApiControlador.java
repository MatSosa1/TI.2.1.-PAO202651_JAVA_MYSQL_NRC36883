package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Datos.ResultadoImportacion;
import ec.edu.monster.modelo.Datos.ResumenImportacion;
import ec.edu.monster.modelo.Formatos;
import ec.edu.monster.modelo.PaisServicio;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.ConsultaApiVista;

/** ECUD3 Consultar País en la API, ECUD2 Importar País e «Importar todos». */
public class ConsultaApiControlador extends ControladorConSesion {

    private final ConsultaApiVista vista;
    private final PaisServicio servicio;

    public ConsultaApiControlador(ConsultaApiVista vista, PaisServicio servicio, Sesion sesion,
            Runnable alSesionNoValida) {
        super(sesion, alSesionNoValida);
        this.vista = vista;
        this.servicio = servicio;
        vista.alConsultar(this::consultar);
        vista.alGuardar(this::guardar);
        vista.alImportarTodos(this::importarTodos);
    }

    private void consultar(String nombre) {
        // ECUD3 alterno 3: nombre vacío o de más de 100 caracteres
        String error = Formatos.errorNombre(nombre);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        ejecutar(() -> vista.mostrarResultados(servicio.consultarApi(sesion.getToken(), nombre.trim()).datos()),
                vista::mostrarError);
    }

    /** ECUD2 pasos 4 a 9 (la vista ya pidió confirmación). */
    private void guardar(String consulta, String nombre) {
        ejecutar(() -> {
            Resultado<ResultadoImportacion> resultado = servicio.importar(sesion.getToken(), consulta, nombre);
            String accion = resultado.datos() == null ? "" : " (" + resultado.datos().accion() + ")";
            vista.mostrarMensaje(resultado.mensaje() + accion);
            vista.marcarGuardado(nombre);
        }, vista::mostrarError);
    }

    private void importarTodos() {
        ejecutar(() -> {
            Resultado<ResumenImportacion> resultado = servicio.importarTodos(sesion.getToken());
            ResumenImportacion resumen = resultado.datos();
            vista.mostrarMensaje(resumen == null ? resultado.mensaje()
                    : resultado.mensaje() + ": " + resumen.recibidos() + " recibidos, " + resumen.registrados()
                    + " registrados, " + resumen.actualizados() + " actualizados");
        }, vista::mostrarError);
    }
}
