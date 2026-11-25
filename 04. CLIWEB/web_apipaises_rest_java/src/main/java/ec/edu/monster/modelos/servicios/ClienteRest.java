package ec.edu.monster.modelos.servicios;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.config.ApiConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Llama a los servicios RESTful del servidor y lee la respuesta estructurada
 * {estado, mensaje, datos} (ECUD10). Con estado OK devuelve el mensaje y los datos; con
 * ERROR, sin conexión o con una respuesta ilegible lanza {@link ServicioException}.
 */
public class ClienteRest {

    public static final Duration ESPERA = Duration.ofSeconds(20);
    /** Importar todos consulta varias páginas de la API y guarda cerca de 250 países. */
    public static final Duration ESPERA_IMPORTACION = Duration.ofSeconds(90);

    private static final Gson GSON = new Gson();

    private final String urlBase;
    private final Transporte transporte;

    /** Cliente real, con la dirección de cliente.properties. */
    public ClienteRest() {
        this(ApiConfig.getUrlBase(), new TransporteHttp());
    }

    public ClienteRest(String urlBase, Transporte transporte) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.transporte = transporte;
    }

    /** Respuesta JSON tal como la envía el servidor. */
    private static final class RespuestaJson<T> {
        String estado;
        String mensaje;
        T datos;
    }

    /**
     * @param token  token de la sesión, o null en el inicio de sesión
     * @param cuerpo objeto a enviar como JSON, o null si la petición no lleva cuerpo
     */
    public <T> Resultado<T> enviar(String metodo, String ruta, String token, Object cuerpo, Type tipoDatos,
            Duration espera) throws ServicioException {
        Map<String, String> cabeceras = new HashMap<>();
        cabeceras.put("Accept", "application/json");
        if (cuerpo != null) {
            cabeceras.put("Content-Type", "application/json; charset=utf-8");
        }
        if (token != null) {
            cabeceras.put("Authorization", token);
        }

        Transporte.Respuesta http;
        try {
            http = transporte.enviar(metodo, urlBase + ruta, cuerpo == null ? null : GSON.toJson(cuerpo),
                    cabeceras, espera);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServicioException(ServicioException.SIN_SERVIDOR);
        } catch (IOException | IllegalArgumentException e) {
            // Sin red, servidor apagado, tiempo agotado o dirección mal escrita
            throw new ServicioException(ServicioException.SIN_SERVIDOR);
        }

        if (http.codigo() == 401 && token != null) {
            // ECUD8: token rechazado; el inicio de sesión (sin token) responde 401 por credenciales
            throw new ServicioException(ServicioException.SESION_NO_VALIDA, true);
        }
        RespuestaJson<T> respuesta;
        try {
            Type tipo = TypeToken.getParameterized(RespuestaJson.class, tipoDatos).getType();
            respuesta = GSON.fromJson(http.cuerpo(), tipo);
        } catch (JsonParseException e) {
            respuesta = null;
        }
        if (respuesta == null || respuesta.estado == null) {
            throw new ServicioException(ServicioException.RESPUESTA_INCOMPLETA);
        }
        String mensaje = respuesta.mensaje == null ? "" : respuesta.mensaje;
        if (!"OK".equals(respuesta.estado)) {
            throw new ServicioException(mensaje.isBlank() ? ServicioException.RESPUESTA_INCOMPLETA : mensaje);
        }
        return new Resultado<>(mensaje, respuesta.datos);
    }

    /** Texto como segmento de la ruta o valor de un parámetro (espacios y tildes codificados). */
    public static String codificar(String texto) {
        return URLEncoder.encode(texto.trim(), StandardCharsets.UTF_8).replace("+", "%20");
    }
}
