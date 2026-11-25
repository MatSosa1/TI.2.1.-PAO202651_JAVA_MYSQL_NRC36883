package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.Formatos;
import ec.edu.monster.modelos.dto.Resultados;
import ec.edu.monster.modelos.servicios.PaisService;
import ec.edu.monster.modelos.servicios.Resultado;
import ec.edu.monster.modelos.servicios.ServicioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ECUD5 Buscar Países (y ECUD6: al cambiar la región el formulario se envía solo).
 * GET /busqueda                                   → formulario vacío
 * GET /busqueda?buscar=1&nombre=&capital=&region= → resultados y total
 */
@WebServlet(name = "BusquedaController", urlPatterns = {"/busqueda"})
public class BusquedaController extends ControladorBase {

    private static final String VISTA = "busqueda.jsp";

    private final transient PaisService paisService;

    public BusquedaController() {
        this(new PaisService());
    }

    BusquedaController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = Formatos.limpiar(request.getParameter("nombre"));
        String capital = Formatos.limpiar(request.getParameter("capital"));
        String region = Formatos.region(request.getParameter("region"));
        request.setAttribute("nombre", nombre);
        request.setAttribute("capital", capital);
        request.setAttribute("region", region);
        request.setAttribute("regiones", Formatos.REGIONES);

        if (request.getParameter("buscar") != null) {
            if (nombre.length() > Formatos.MAX_NOMBRE || capital.length() > Formatos.MAX_NOMBRE) {
                request.setAttribute("error", Formatos.NOMBRE_MUY_LARGO);
            } else {
                try {
                    Resultado<Resultados.Busqueda> resultado =
                            paisService.buscar(token(request), nombre, capital, region);
                    request.setAttribute("mensaje", resultado.mensaje());
                    request.setAttribute("resultados", resultado.datos().getPaises());
                    request.setAttribute("total", Formatos.total(resultado.datos().getPaises().size()));
                } catch (ServicioException e) {
                    if (sesionVencida(e, request, response)) {
                        return;
                    }
                    request.setAttribute("error", e.getMessage());
                }
            }
        }
        render(request, response, VISTA);
    }
}
