package ec.edu.monster.modelos.data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Rutas de los servicios del servidor, relativas a la dirección base (…/api). */
public final class Endpoints {

  public static final String LOGIN = "/Login/login";
  public static final String LOGOUT = "/Login/logout";
  public static final String PAISES = "/Paises";
  public static final String BUSCAR = "/Paises/buscar";
  public static final String EXTERNO = "/Paises/externo/";
  public static final String IMPORTAR = "/Paises/importar";
  public static final String IMPORTAR_TODOS = "/Paises/importar/todos";
  public static final String REPORTES = "/Reportes";

  private Endpoints() {
  }

  /** País como segmento de la ruta (espacios y tildes codificados). */
  public static String segmento(String texto) {
    return codificar(texto);
  }

  /** Parámetro de consulta «&amp;nombre=valor», o vacío si no tiene valor. */
  public static String parametro(String nombre, String valor) {
    return valor == null || valor.isBlank() ? "" : "&" + nombre + "=" + codificar(valor.trim());
  }

  /** Une los parámetros: el primero lleva «?» en lugar de «&amp;». */
  public static String consulta(String... parametros) {
    String unidos = String.join("", parametros);
    return unidos.isEmpty() ? "" : "?" + unidos.substring(1);
  }

  private static String codificar(String texto) {
    return URLEncoder.encode(texto.trim(), StandardCharsets.UTF_8).replace("+", "%20");
  }
}
