package ec.edu.monster.controlador;

import ec.edu.monster.modelos.data.Datos.Busqueda;
import ec.edu.monster.modelos.data.Datos.Importacion;
import ec.edu.monster.modelos.data.Datos.Informe;
import ec.edu.monster.modelos.data.Datos.Pais;
import ec.edu.monster.modelos.data.Datos.Resumen;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.repositorios.ApiPaisesRepositorio;
import ec.edu.monster.servicios.SesionServicio;
import java.util.Collections;
import java.util.List;

/**
 * Operaciones con países: Gestionar (ECUD4), Buscar y Filtrar (ECUD5, ECUD6), Consultar en la API
 * (ECUD3), Importar (ECUD2) e Informes (ECUD7). Todas se ejecutan en segundo plano y nunca lanzan
 * excepciones: devuelven {@link Resultado}.
 */
public class PaisesControlador extends ControladorConSesion {

    private final ApiPaisesRepositorio repositorio;

    public PaisesControlador(ApiPaisesRepositorio repositorio, SesionServicio sesion) {
        super(sesion);
        this.repositorio = repositorio;
    }

    /**
     * Listar (sin criterios) o Buscar/Filtrar. Devuelve siempre la lista de países encontrada.
     *
     * @param region región de la lista, o "" / null para todas
     */
    public Resultado<List<Pais>> buscar(String nombre, String capital, String region) {
        String error = ValidadorDatos.errorCriterio(nombre);
        if (error == null) {
            error = ValidadorDatos.errorCriterio(capital);
        }
        if (error != null) {
            return Resultado.error(error);
        }
        String regionValida = ValidadorDatos.REGIONES.contains(ValidadorDatos.limpiar(region))
                ? ValidadorDatos.limpiar(region) : "";
        boolean sinCriterios = ValidadorDatos.limpiar(nombre).isEmpty()
                && ValidadorDatos.limpiar(capital).isEmpty() && regionValida.isEmpty();
        if (sinCriterios) {
            return revisarSesion(repositorio.listar(getToken()));
        }
        Resultado<Busqueda> resultado = revisarSesion(repositorio.buscar(getToken(), nombre, capital, regionValida));
        if (!resultado.isExito()) {
            return copiarError(resultado);
        }
        List<Pais> paises = resultado.getDatos() == null ? Collections.<Pais>emptyList() : resultado.getDatos().getPaises();
        return Resultado.ok(paises, resultado.getMensaje());
    }

    public Resultado<Pais> actualizar(String nombre) {
        return revisarSesion(repositorio.actualizar(getToken(), nombre));
    }

    public Resultado<Object> eliminar(String nombre) {
        return revisarSesion(repositorio.eliminar(getToken(), nombre));
    }

    /** ECUD3 (y ECUD4 Registrar, pasos 2 a 4). */
    public Resultado<List<Pais>> consultarApi(String nombre) {
        String error = ValidadorDatos.errorNombre(nombre);
        if (error != null) {
            return Resultado.error(error);
        }
        return revisarSesion(repositorio.consultarApi(getToken(), nombre.trim()));
    }

    /** ECUD4 Registrar (409 si ya existe). */
    public Resultado<Pais> registrar(String consulta, String nombre) {
        return revisarSesion(repositorio.registrar(getToken(), consulta, nombre));
    }

    /** ECUD2: el mensaje incluye si el país fue «registrado» o «actualizado». */
    public Resultado<Importacion> importar(String consulta, String nombre) {
        Resultado<Importacion> resultado = revisarSesion(repositorio.importar(getToken(), consulta, nombre));
        if (resultado.isExito() && resultado.getDatos() != null && resultado.getDatos().getAccion() != null) {
            return Resultado.ok(resultado.getDatos(), resultado.getMensaje() + " (" + resultado.getDatos().getAccion() + ")");
        }
        return resultado;
    }

    /** Importar todos: el mensaje incluye el resumen. */
    public Resultado<Resumen> importarTodos() {
        Resultado<Resumen> resultado = revisarSesion(repositorio.importarTodos(getToken()));
        Resumen resumen = resultado.getDatos();
        if (resultado.isExito() && resumen != null) {
            return Resultado.ok(resumen, resultado.getMensaje() + ": " + resumen.getRecibidos() + " recibidos, "
                    + resumen.getRegistrados() + " registrados, " + resumen.getActualizados() + " actualizados");
        }
        return resultado;
    }

    /** ECUD7. */
    public Resultado<Informe> informes() {
        return revisarSesion(repositorio.informes(getToken()));
    }

    private static <T> Resultado<T> copiarError(Resultado<?> original) {
        return original.isSesionExpirada()
                ? Resultado.<T>sesionExpirada(original.getMensaje())
                : Resultado.<T>error(original.getMensaje());
    }
}
