package ec.edu.monster.vista;

import ec.edu.monster.modelos.dto.RespuestaEstructurada;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * «Vista» del servidor según el diagrama de arquitectura MVC: convierte el resultado de un
 * controlador en una respuesta HTTP JSON con la estructura estado / mensaje / datos y el
 * código HTTP correspondiente (ECUD10). La serialización la hace JSON-B (Payara).
 */
public final class JsonResponseView {

    /** JSON en UTF-8 explícito, para que los clientes lean bien las tildes. */
    public static final MediaType JSON = MediaType.APPLICATION_JSON_TYPE.withCharset("UTF-8");

    private JsonResponseView() {
    }

    /** 200 con estado OK. */
    public static Response ok(String mensaje, Object datos) {
        return construir(Response.Status.OK.getStatusCode(), RespuestaEstructurada.ok(mensaje, datos));
    }

    /** 201 con estado OK (ECUD4, Registrar). */
    public static Response creado(String mensaje, Object datos) {
        return construir(Response.Status.CREATED.getStatusCode(), RespuestaEstructurada.ok(mensaje, datos));
    }

    /** Estado ERROR, sin datos, con el código HTTP indicado. */
    public static Response error(int codigoHttp, String mensaje) {
        return construir(codigoHttp, RespuestaEstructurada.error(mensaje));
    }

    private static Response construir(int codigoHttp, RespuestaEstructurada<?> respuesta) {
        return Response.status(codigoHttp).type(JSON).entity(respuesta).build();
    }
}
