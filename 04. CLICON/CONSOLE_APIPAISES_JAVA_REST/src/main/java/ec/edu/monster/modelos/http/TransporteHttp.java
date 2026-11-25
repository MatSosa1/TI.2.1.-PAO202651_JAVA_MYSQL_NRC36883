package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/** Transporte real con java.net.http. */
public class TransporteHttp implements Transporte {

  private final HttpClient http = HttpClient.newBuilder()
    .version(HttpClient.Version.HTTP_1_1)
    .connectTimeout(ConfigHTTP.TIMEOUT)
    .build();

  @Override
  public RespuestaHttp enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
      throws IOException, InterruptedException {
    HttpRequest.BodyPublisher publicador = cuerpo == null
      ? HttpRequest.BodyPublishers.noBody()
      : HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8);

    HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(url))
      .timeout(espera)
      .method(metodo, publicador);
    cabeceras.forEach(peticion::header);

    HttpResponse<String> respuesta = http.send(
      peticion.build(),
      HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
    );
    return new RespuestaHttp(respuesta.statusCode(), respuesta.body());
  }
}
