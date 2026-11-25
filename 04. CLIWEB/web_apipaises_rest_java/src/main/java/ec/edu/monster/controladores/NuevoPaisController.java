package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.Formatos;
import ec.edu.monster.modelos.dto.Pais;
import ec.edu.monster.modelos.servicios.PaisService;
import ec.edu.monster.modelos.servicios.Resultado;
import ec.edu.monster.modelos.servicios.ServicioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ECUD4 Registrar País.
 * GET  /paises/nuevo                  → campo «Nombre del país»
 * GET  /paises/nuevo?consulta=…       → resultados de la API para elegir (pasos 3 y 4)
 * POST /paises/nuevo consulta, nombre → registra (409 si ya existe) y vuelve a la tabla (PRG)
 */
@WebServlet(name = "NuevoPaisController", urlPatterns = {"/paises/nuevo"})
public class NuevoPaisController extends ControladorBase {

    private static final String VISTA = "nuevo.jsp";

    private final transient PaisService paisService;

    public NuevoPaisController() {
        this(new PaisService());
    }

    NuevoPaisController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String consulta = request.getParameter("consulta");
        if (consulta != null && consultar(request, response, consulta)) {
            return;
        }
        render(request, response, VISTA);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String consulta = Formatos.limpiar(request.getParameter("consulta"));
        String nombre = Formatos.limpiar(request.getParameter("nombre"));
        if (nombre.isEmpty()) {
            request.setAttribute("error", "Seleccione un país de los resultados");
            if (!consultar(request, response, consulta)) {
                render(request, response, VISTA);
            }
            return;
        }
        try {
            Resultado<Pais> resultado = paisService.registrar(token(request), consulta, nombre);
            // ECUD4 Registrar paso 9: mensaje, se cierra el formulario y se recarga la tabla
            flash(request, resultado.mensaje(), false);
            redirigir(request, response, "/paises");
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return;
            }
            // Duplicado, API no disponible...: se muestra el error con los resultados otra vez
            if (consultar(request, response, consulta)) {
                return;
            }
            request.setAttribute("error", e.getMessage());
            render(request, response, VISTA);
        }
    }

    /**
     * Consulta la API y deja los resultados en la petición. Devuelve true solo si ya respondió
     * (redirección por sesión vencida); los errores quedan en el atributo «error».
     */
    private boolean consultar(HttpServletRequest request, HttpServletResponse response, String consulta)
            throws IOException {
        request.setAttribute("consulta", consulta);
        String error = Formatos.errorNombre(consulta);
        if (error != null) {
            request.setAttribute("error", error);
            return false;
        }
        try {
            request.setAttribute("resultados", paisService.consultarApi(token(request), consulta.trim()).datos());
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return true;
            }
            request.setAttribute("error", e.getMessage());
        }
        return false;
    }
}
