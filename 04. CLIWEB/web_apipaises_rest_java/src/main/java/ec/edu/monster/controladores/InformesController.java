package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.Informe;
import ec.edu.monster.modelos.servicios.ReporteService;
import ec.edu.monster.modelos.servicios.Resultado;
import ec.edu.monster.modelos.servicios.ServicioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** ECUD7 Generar Informes: GET /informes → tablas, país más poblado y gráfico de barras. */
@WebServlet(name = "InformesController", urlPatterns = {"/informes"})
public class InformesController extends ControladorBase {

    private static final String VISTA = "informes.jsp";
    /** Mensaje del servidor sin países guardados (ECUD7, alterno 2). */
    static final String SIN_PAISES = "No hay países registrados para generar informes";

    private final transient ReporteService reporteService;

    public InformesController() {
        this(new ReporteService());
    }

    InformesController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Resultado<Informe> resultado = reporteService.generar(token(request));
            request.setAttribute("informe", resultado.datos());
            request.setAttribute("mensaje", resultado.mensaje());
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return;
            }
            request.setAttribute("error", e.getMessage());
            // ECUD7 alterno 2: se sugiere guardar países
            request.setAttribute("sinPaises", SIN_PAISES.equals(e.getMessage()));
        }
        render(request, response, VISTA);
    }
}
