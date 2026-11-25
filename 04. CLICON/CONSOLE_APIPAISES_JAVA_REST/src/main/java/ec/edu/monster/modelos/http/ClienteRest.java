package ec.edu.monster.modelos.http;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.controlador.ErroresCliente;
import ec.edu.monster.modelos.dto.Respuesta;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Llama a los servicios RESTful del servidor y convierte la respuesta JSON en
 * {@link Respuesta}. Nunca lanza excepciones: sin conexión o con una respuesta ilegible
 * devuelve un error con el mensaje para el usuario (ECUD10, alterno 4).
 */
public final class ClienteRest {

  private static final Gson GSON = new Gson();

  private ClienteRest() {
  }

  public static <T> Respuesta<T> get(String ruta, Type tipoDatos) {
    return enviar("GET", ruta, null, tipoDatos, ConfigHTTP.TIMEOUT);
  }

  public static <T> Respuesta<T> post(String ruta, Object cuerpo, Type tipoDatos) {
    return enviar("POST", ruta, cuerpo, tipoDatos, ConfigHTTP.TIMEOUT);
  }

  public static <T> Respuesta<T> post(String ruta, Object cuerpo, Type tipoDatos, Duration espera) {
    return enviar("POST", ruta, cuerpo, tipoDatos, espera);
  }

  public static <T> Respuesta<T> put(String ruta, Type tipoDatos) {
    return enviar("PUT", ruta, null, tipoDatos, ConfigHTTP.TIMEOUT);
  }

  public static <T> Respuesta<T> delete(String ruta, Type tipoDatos) {
    return enviar("DELETE", ruta, null, tipoDatos, ConfigHTTP.TIMEOUT);
  }

  private static <T> Respuesta<T> enviar(String metodo, String ruta, Object cuerpo, Type tipoDatos, Duration espera) {
    Map<String, String> cabeceras = new HashMap<>();
    cabeceras.put("Accept", "application/json");
    if (cuerpo != null)
      cabeceras.put("Content-Type", "application/json; charset=utf-8");
    if (ConfigHTTP.haySesion())
      cabeceras.put("Authorization", ConfigHTTP.getToken());

    try {
      RespuestaHttp http = ConfigHTTP.getTransporte().enviar(metodo, ConfigHTTP.getBaseUrl() + ruta,
        cuerpo == null ? null : GSON.toJson(cuerpo), cabeceras, espera);

      Type tipo = TypeToken.getParameterized(Respuesta.class, tipoDatos).getType();
      Respuesta<T> respuesta = GSON.fromJson(http.cuerpo(), tipo);
      if (respuesta == null || respuesta.getEstado() == null)
        return conCodigo(Respuesta.error(ErroresCliente.RESPUESTA_INCOMPLETA), http.codigo());
      return conCodigo(respuesta, http.codigo());
    } catch (JsonParseException e) {
      return Respuesta.error(ErroresCliente.RESPUESTA_INCOMPLETA);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return Respuesta.error(ErroresCliente.SIN_SERVIDOR);
    } catch (IOException | IllegalArgumentException e) {
      // Sin red, servidor apagado, tiempo agotado o dirección mal escrita
      return Respuesta.error(ErroresCliente.SIN_SERVIDOR);
    }
  }

  private static <T> Respuesta<T> conCodigo(Respuesta<T> respuesta, int codigo) {
    respuesta.setCodigoHttp(codigo);
    return respuesta;
  }
}
