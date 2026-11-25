package ec.edu.monster.modelos.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Dirección del servidor y datos de la sesión. El token se guarda mientras dure la sesión
 * (ECUD1, paso 7) y viaja en el encabezado Authorization.
 *
 * <p>La dirección se toma, en este orden, del argumento del programa, de la propiedad
 * {@code -Dservidor.url} o de {@code cliente.properties}.
 */
public final class ConfigHTTP {

  /** Espera máxima de una respuesta del servidor. */
  public static final Duration TIMEOUT = Duration.ofSeconds(20);
  /** Importar todos consulta varias páginas de la API y guarda cerca de 250 países (ERS: máx. 30 s). */
  public static final Duration TIMEOUT_IMPORTACION = Duration.ofSeconds(90);

  private static final String URL_POR_DEFECTO = "http://localhost:8080/WS_APIPAISES_JAVA_REST/api";

  private static String baseUrl = leerUrl();
  private static String token = "";
  private static String nombre = "";
  private static Transporte transporte;

  private ConfigHTTP() {
  }

  public static String getBaseUrl() {
    return baseUrl;
  }

  public static void setBaseUrl(String baseUrl) {
    String limpia = baseUrl.trim();
    ConfigHTTP.baseUrl = limpia.endsWith("/") ? limpia.substring(0, limpia.length() - 1) : limpia;
  }

  public static String getToken() {
    return token;
  }

  public static void setToken(String token) {
    ConfigHTTP.token = token == null ? "" : token;
  }

  /** Nombre del usuario que devolvió el inicio de sesión. */
  public static String getNombre() {
    return nombre;
  }

  public static void setNombre(String nombre) {
    ConfigHTTP.nombre = nombre == null ? "" : nombre;
  }

  public static Transporte getTransporte() {
    if (transporte == null)
      transporte = new TransporteHttp();
    return transporte;
  }

  public static void setTransporte(Transporte transporte) {
    ConfigHTTP.transporte = transporte;
  }

  public static boolean haySesion() {
    return !token.isEmpty();
  }

  /** Borra los datos de la sesión (cerrar sesión o token no válido). */
  public static void limpiar() {
    token = "";
    nombre = "";
  }

  private static String leerUrl() {
    String propiedad = System.getProperty("servidor.url");
    if (propiedad != null && !propiedad.isBlank())
      return sinBarraFinal(propiedad.trim());

    Properties archivo = new Properties();
    try (InputStream entrada = ConfigHTTP.class.getResourceAsStream("/cliente.properties")) {
      if (entrada != null)
        archivo.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
    } catch (IOException e) {
      // Sin archivo se usa la dirección por defecto
    }
    return sinBarraFinal(archivo.getProperty("servidor.url", URL_POR_DEFECTO).trim());
  }

  private static String sinBarraFinal(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }
}
