package ec.edu.monster.controladores;

import ec.edu.monster.dao.PaisDAO;
import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.modelos.dto.InformeEstadistico;
import ec.edu.monster.vista.JsonResponseView;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Informes estadísticos (RQF-007, ECUD7). No dependen de la API pública. */
@Path("Reportes")
@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
public class ReportesController extends ControladorBase {

    private final PaisDAO paisDAO = new PaisDAO();

    /**
     * GET /api/Reportes: países por región, país más poblado y promedio poblacional por región.
     * Sin países guardados responde estado ERROR con HTTP 200, como indica el diagrama de
     * secuencia Generar_Informes.
     */
    @GET
    public Response generar() {
        return responder("generarInformes", () -> {
            if (paisDAO.contar() == 0) {
                return JsonResponseView.error(200, Mensajes.SIN_PAISES_INFORMES);
            }
            InformeEstadistico informe = new InformeEstadistico(paisDAO.contarPorRegion(),
                    paisDAO.paisMasPoblado(), paisDAO.promedioPorRegion());
            return JsonResponseView.ok(Mensajes.INFORMES_GENERADOS, informe);
        });
    }
}
