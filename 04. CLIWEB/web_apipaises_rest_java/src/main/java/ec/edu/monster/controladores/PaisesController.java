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
import java.util.List;

/**
 * ECUD4 Gestionar Países y ECUD6 Filtrar.
 * GET  /paises[?region=Europa]                          → tabla (con el filtro de región activo)
 * POST /paises accion=actualizar nombre                  → el servidor vuelve a consultar la API (PRG)
 * POST /paises accion=eliminar paso=revisar nombre       → confirmación «¿Eliminar el país …?»
 * POST /paises accion=eliminar paso=confirmar nombre     → elimina y vuelve a la tabla (PRG)
 */
@WebServlet(name = "PaisesController", urlPatterns = {"/paises"})
public class PaisesController extends ControladorBase {

    private static final String VISTA = "paises.jsp";
    private static final String RUTA = "/paises";

    private final transient PaisService paisService;

    public PaisesController() {
        this(new PaisService());
    }

    PaisesController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        leerFlash(request);
        String region = Formatos.region(request.getParameter("region"));
        request.setAttribute("region", region);
        request.setAttribute("regiones", Formatos.REGIONES);
        try {
            List<Pais> paises = region.isEmpty()
                    ? paisService.listar(token(request)).datos()
                    : paisService.buscar(token(request), null, null, region).datos().getPaises();
            paises = paises == null ? List.of() : paises;
            request.setAttribute("paises", paises);
            request.setAttribute("total", Formatos.total(paises.size()));
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return;
            }
            request.setAttribute("error", e.getMessage());
            request.setAttribute("paises", List.of());
        }
        render(request, response, VISTA);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        String nombre = Formatos.limpiar(request.getParameter("nombre"));
        String region = Formatos.region(request.getParameter("region"));
        String volver = RUTA + (region.isEmpty() ? "" : "?region=" + codificar(region));

        if (nombre.isEmpty()) {
            redirigir(request, response, volver);
            return;
        }
        if ("eliminar".equals(accion) && !"confirmar".equals(request.getParameter("paso"))) {
            // ECUD4 Eliminar pasos 2 y 3: se pide confirmar antes de enviar
            request.setAttribute("confirmarEliminar", nombre);
            request.setAttribute("region", region);
            render(request, response, VISTA);
            return;
        }
        try {
            Resultado<?> resultado = "eliminar".equals(accion)
                    ? paisService.eliminar(token(request), nombre)
                    : paisService.actualizar(token(request), nombre);
            flash(request, resultado.mensaje(), false);
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return;
            }
            flash(request, e.getMessage(), true);
        }
        // Post/Redirect/Get: recargar la página no repite la operación
        redirigir(request, response, volver);
    }

    static String codificar(String texto) {
        return java.net.URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8);
    }
}
