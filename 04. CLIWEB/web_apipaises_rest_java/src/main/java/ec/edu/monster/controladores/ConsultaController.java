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
 * ECUD3 Consultar País en la API, ECUD2 Importar e «Importar todos».
 * GET  /consulta[?nombre=…]                                   → campo y tabla de resultados
 * POST /consulta accion=guardar paso=revisar consulta, nombre → «¿Guardar el país … en la base de datos local?»
 * POST /consulta accion=guardar paso=confirmar               → importa y vuelve a la consulta (PRG)
 * POST /consulta accion=importarTodos paso=revisar|confirmar → confirmación e importación total
 */
@WebServlet(name = "ConsultaController", urlPatterns = {"/consulta"})
public class ConsultaController extends ControladorBase {

    private static final String VISTA = "consulta.jsp";
    private static final String RUTA = "/consulta";

    private final transient PaisService paisService;

    public ConsultaController() {
        this(new PaisService());
    }

    ConsultaController(PaisService paisService) {
        this.paisService = paisService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        leerFlash(request);
        String nombre = request.getParameter("nombre");
        if (nombre != null) {
            request.setAttribute("nombre", nombre);
            String error = Formatos.errorNombre(nombre);
            if (error != null) {
                request.setAttribute("error", error);
            } else {
                try {
                    request.setAttribute("resultados", paisService.consultarApi(token(request), nombre.trim()).datos());
                } catch (ServicioException e) {
                    if (sesionVencida(e, request, response)) {
                        return;
                    }
                    // ECUD3 alterno 2: se conserva el texto para corregirlo
                    request.setAttribute("error", e.getMessage());
                }
            }
        }
        render(request, response, VISTA);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        String consulta = Formatos.limpiar(request.getParameter("consulta"));
        String nombre = Formatos.limpiar(request.getParameter("nombre"));
        boolean confirmar = "confirmar".equals(request.getParameter("paso"));
        String volver = RUTA + (consulta.isEmpty() ? "" : "?nombre=" + PaisesController.codificar(consulta));

        if ("importarTodos".equals(accion)) {
            if (!confirmar) {
                request.setAttribute("confirmar", "importarTodos");
                request.setAttribute("consulta", consulta);
                render(request, response, VISTA);
                return;
            }
            ejecutar(request, response, volver, () -> {
                Resultado<Resultados.Resumen> resultado = paisService.importarTodos(token(request));
                Resultados.Resumen resumen = resultado.datos();
                return resumen == null ? resultado.mensaje()
                        : resultado.mensaje() + ": " + resumen.getRecibidos() + " recibidos, "
                        + resumen.getRegistrados() + " registrados, " + resumen.getActualizados() + " actualizados";
            });
            return;
        }

        if (nombre.isEmpty()) {
            redirigir(request, response, volver);
            return;
        }
        if (!confirmar) {
            // ECUD2 pasos 2 y 3: confirmación antes de guardar
            request.setAttribute("confirmar", "guardar");
            request.setAttribute("consulta", consulta);
            request.setAttribute("pais", nombre);
            render(request, response, VISTA);
            return;
        }
        ejecutar(request, response, volver, () -> {
            Resultado<Resultados.Importacion> resultado = paisService.importar(token(request), consulta, nombre);
            String accionRealizada = resultado.datos() == null ? "" : " (" + resultado.datos().getAccion() + ")";
            return resultado.mensaje() + accionRealizada;
        });
    }

    @FunctionalInterface
    private interface Operacion {
        String ejecutar() throws ServicioException;
    }

    /** Ejecuta la operación, deja el mensaje como flash y redirige (Post/Redirect/Get). */
    private static void ejecutar(HttpServletRequest request, HttpServletResponse response, String volver,
            Operacion operacion) throws IOException {
        try {
            flash(request, operacion.ejecutar(), false);
        } catch (ServicioException e) {
            if (sesionVencida(e, request, response)) {
                return;
            }
            flash(request, e.getMessage(), true);
        }
        redirigir(request, response, volver);
    }
}
