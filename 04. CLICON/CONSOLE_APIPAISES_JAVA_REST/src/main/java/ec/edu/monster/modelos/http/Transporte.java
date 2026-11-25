package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/** Envía una petición HTTP. Las pruebas de pruebas/ lo reemplazan por un servidor simulado. */
public interface Transporte {

  /**
   * @param metodo    GET, POST, PUT o DELETE
   * @param cuerpo    JSON a enviar, o null si la petición no lleva cuerpo
   * @param cabeceras encabezados adicionales (Authorization, Content-Type...)
   * @param espera    tiempo máximo de espera de la respuesta
   */
  RespuestaHttp enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
      throws IOException, InterruptedException;
}
