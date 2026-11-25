package ec.edu.monster.vista;

import ec.edu.monster.modelos.Mensajes;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Última red de seguridad (ECUD10, alterno 1): cualquier excepción no prevista se registra en
 * el log y se responde con la estructura uniforme, sin exponer sentencias SQL, excepciones de
 * Java ni trazas. También da formato a los errores del propio protocolo (ruta inexistente,
 * método no permitido, JSON mal formado).
 */
@Provider
public class ErrorInesperadoMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ErrorInesperadoMapper.class.getName());

    @Override
    public Response toResponse(Throwable error) {
        if (error instanceof NotFoundException) {
            return JsonResponseView.error(404, Mensajes.RECURSO_NO_ENCONTRADO);
        }
        if (error instanceof NotAllowedException) {
            return JsonResponseView.error(405, Mensajes.METODO_NO_PERMITIDO);
        }
        if (esJsonMalFormado(error)) {
            return JsonResponseView.error(400, Mensajes.DATOS_NO_VALIDOS);
        }
        if (error instanceof WebApplicationException web && web.getResponse().getStatus() < 500) {
            int codigo = web.getResponse().getStatus();
            return JsonResponseView.error(codigo, codigo == 400 ? Mensajes.DATOS_NO_VALIDOS : Mensajes.ERROR_INESPERADO);
        }
        LOG.log(Level.SEVERE, "Error inesperado", error);
        return JsonResponseView.error(500, Mensajes.ERROR_INESPERADO);
    }

    private static boolean esJsonMalFormado(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof JsonbException || causa instanceof jakarta.json.JsonException) {
                return true;
            }
        }
        return false;
    }
}
