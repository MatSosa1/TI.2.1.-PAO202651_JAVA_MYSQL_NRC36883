package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Datos.Pais;
import ec.edu.monster.modelo.Datos.ResultadoBusqueda;
import ec.edu.monster.modelo.Formatos;
import ec.edu.monster.modelo.PaisServicio;
import ec.edu.monster.modelo.Resultado;
import ec.edu.monster.modelo.ServicioException;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.vista.Vistas.PaisesVista;
import java.util.List;

/**
 * ECUD4 Gestionar Países (Listar, Registrar, Actualizar, Eliminar) y ECUD6 Filtrar. La región
 * elegida queda activa: después de registrar, actualizar o eliminar se recarga con ese filtro.
 */
public class PaisesControlador extends ControladorConSesion {

    private final PaisesVista vista;
    private final PaisServicio servicio;
    private volatile String region = "";

    public PaisesControlador(PaisesVista vista, PaisServicio servicio, Sesion sesion, Runnable alSesionNoValida) {
        super(sesion, alSesionNoValida);
        this.vista = vista;
        this.servicio = servicio;
        vista.alCargar(this::cargar);
        vista.alActualizar(this::actualizar);
        vista.alEliminar(this::eliminar);
        vista.alConsultarApi(this::consultarApi);
        vista.alRegistrar(this::registrar);
    }

    /** Listar (sin región) o Filtrar (con región). */
    private void cargar(String regionElegida) {
        region = Formatos.region(regionElegida);
        ejecutar(this::recargar, vista::mostrarError);
    }

    private void recargar() throws ServicioException {
        String actual = region;
        List<Pais> paises;
        if (actual.isEmpty()) {
            paises = servicio.listar(sesion.getToken()).datos();
        } else {
            ResultadoBusqueda resultado = servicio.buscar(sesion.getToken(), null, null, actual).datos();
            paises = resultado == null ? null : resultado.paises();
        }
        vista.mostrarPaises(paises == null ? List.of() : paises, actual);
    }

    /** ECUD4 Actualizar: el servidor vuelve a consultar la API. */
    private void actualizar(String nombre) {
        ejecutar(() -> {
            Resultado<Pais> resultado = servicio.actualizar(sesion.getToken(), nombre);
            vista.mostrarMensaje(resultado.mensaje());
            recargar();
        }, vista::mostrarError);
    }

    /** ECUD4 Eliminar (la vista ya pidió confirmación). */
    private void eliminar(String nombre) {
        ejecutar(() -> {
            Resultado<Object> resultado = servicio.eliminar(sesion.getToken(), nombre);
            vista.mostrarMensaje(resultado.mensaje());
            recargar();
        }, vista::mostrarError);
    }

    /** ECUD4 Registrar, pasos 3 y 4: resultados de la API para elegir el país. */
    private void consultarApi(String consulta) {
        String error = Formatos.errorNombre(consulta);
        if (error != null) {
            vista.mostrarErrorRegistro(error);
            return;
        }
        ejecutar(() -> vista.mostrarResultadosApi(servicio.consultarApi(sesion.getToken(), consulta.trim()).datos()),
                vista::mostrarErrorRegistro);
    }

    /** ECUD4 Registrar, pasos 6 a 9 (409 si ya existe). */
    private void registrar(String consulta, String nombre) {
        ejecutar(() -> {
            Resultado<Pais> resultado = servicio.registrar(sesion.getToken(), consulta, nombre);
            vista.registroCompletado(resultado.mensaje());
            recargar();
        }, vista::mostrarErrorRegistro);
    }
}
