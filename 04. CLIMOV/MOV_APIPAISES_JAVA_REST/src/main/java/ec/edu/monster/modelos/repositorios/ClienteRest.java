package ec.edu.monster.modelos.repositorios;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelos.data.Resultado;
import ec.edu.monster.modelos.interfaces.Transporte;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Llama a los servicios RESTful del servidor y convierte la respuesta estructurada
 * {estado, mensaje, datos} (ECUD10) en {@link Resultado}. Nunca lanza excepciones: sin red, sin
 * servidor o con una respuesta ilegible devuelve un error con el mensaje para el usuario.
 */
public class ClienteRest {

    public static final String SIN_SERVIDOR = "No se pudo conectar con el servidor. Verifique la red e intente nuevamente.";
    public static final String RESPUESTA_INCOMPLETA = "El servidor envió una respuesta vacía o incompleta.";
    public static final String SESION_NO_VALIDA = "Su sesión no es válida. Inicie sesión nuevamente.";

    public static final int ESPERA_MS = 20_000;
    /** Importar todos consulta varias páginas de la API y guarda cerca de 250 países. */
    public static final int ESPERA_IMPORTACION_MS = 90_000;

    private static final Gson GSON = new Gson();

    private final String urlBase;
    private final Transporte transporte;
    private final VerificadorRed red;

    public ClienteRest(String urlBase, Transporte transporte, VerificadorRed red) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.transporte = transporte;
        this.red = red;
    }

    public String getUrlBase() {
        return urlBase;
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
            int esperaMs) {
        if (!red.hayRed()) {
            return Resultado.error(SIN_SERVIDOR);
        }
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
                    cabeceras, esperaMs);
        } catch (IOException | RuntimeException e) {
            // Sin conexión, servidor apagado, tiempo agotado o dirección mal escrita
            return Resultado.error(SIN_SERVIDOR);
        }

        if (http.codigo == 401 && token != null) {
            // ECUD8: token rechazado; el inicio de sesión (sin token) responde 401 por credenciales
            return Resultado.sesionExpirada(SESION_NO_VALIDA);
        }
        RespuestaJson<T> respuesta;
        try {
            Type tipo = TypeToken.getParameterized(RespuestaJson.class, tipoDatos).getType();
            respuesta = GSON.fromJson(http.cuerpo, tipo);
        } catch (JsonParseException e) {
            respuesta = null;
        }
        if (respuesta == null || respuesta.estado == null) {
            return Resultado.error(RESPUESTA_INCOMPLETA);
        }
        String mensaje = respuesta.mensaje == null ? "" : respuesta.mensaje;
        if (!"OK".equals(respuesta.estado)) {
            return Resultado.error(mensaje.isEmpty() ? RESPUESTA_INCOMPLETA : mensaje);
        }
        return Resultado.ok(respuesta.datos, mensaje);
    }

    /** Texto como segmento de la ruta o valor de un parámetro (espacios y tildes codificados). */
    public static String codificar(String texto) {
        try {
            return URLEncoder.encode(texto.trim(), "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
