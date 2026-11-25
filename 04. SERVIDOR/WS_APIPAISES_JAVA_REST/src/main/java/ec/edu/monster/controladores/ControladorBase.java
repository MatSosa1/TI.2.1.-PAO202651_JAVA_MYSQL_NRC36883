package ec.edu.monster.controladores;

import ec.edu.monster.modelos.Mensajes;
import ec.edu.monster.servicios.ApiExternaException;
import ec.edu.monster.utilidades.Textos;
import ec.edu.monster.vista.JsonResponseView;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Traducción común de las fallas a la respuesta estructurada (ECUD10):
 * <ul>
 *   <li>falla de la API externa: 502 o 404 con el mensaje del ECUD9;</li>
 *   <li>falla de MySQL: 500 «Servicio no disponible, intente más tarde».</li>
 * </ul>
 * El detalle técnico solo va al log del servidor.
 */
abstract class ControladorBase {

    private static final Logger LOG = Logger.getLogger(ControladorBase.class.getName());

    /** Operación de un controlador que puede fallar por la base de datos o por la API externa. */
    @FunctionalInterface
    interface Accion {
        Response ejecutar() throws SQLException, ApiExternaException;
    }

    protected Response responder(String operacion, Accion accion) {
        try {
            return accion.ejecutar();
        } catch (ApiExternaException e) {
            LOG.log(Level.WARNING, "{0}: {1}", new Object[] {operacion, e.getMessage()});
            return JsonResponseView.error(e.getCodigoHttp(), e.getMensajeUsuario());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, operacion + ": error de base de datos", e);
            return JsonResponseView.error(500, Mensajes.SERVICIO_NO_DISPONIBLE);
        }
    }

    /**
     * Mismas reglas que validan los clientes para el nombre de un país (ECUD3, alterno 3).
     *
     * @return el mensaje de error, o null si el nombre es válido
     */
    protected static String errorNombre(String nombre) {
        if (Textos.vacio(nombre)) {
            return Mensajes.INGRESE_NOMBRE_PAIS;
        }
        if (nombre.trim().length() > Textos.MAX_LONGITUD) {
            return Mensajes.NOMBRE_MUY_LARGO;
        }
        return null;
    }
}
