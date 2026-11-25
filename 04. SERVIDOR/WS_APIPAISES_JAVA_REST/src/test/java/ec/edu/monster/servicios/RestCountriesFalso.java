package ec.edu.monster.servicios;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Servidor HTTP local que imita la API REST Countries v5, para probar ConsumoAPIPaises sin
 * internet ni API key. Cada petición se responde con la función {@code responder}.
 */
final class RestCountriesFalso implements AutoCloseable {

    /** Respuesta que devuelve el servidor falso. */
    record Respuesta(int codigo, String cuerpo, long demoraMs) {
        static Respuesta ok(String cuerpo) {
            return new Respuesta(200, cuerpo, 0);
        }
    }

    /** Petición recibida: ruta, parámetros y encabezado Authorization. */
    record Peticion(String ruta, Map<String, String> parametros, String authorization) {
    }

    private final HttpServer servidor;
    private final List<Peticion> recibidas = Collections.synchronizedList(new ArrayList<>());

    RestCountriesFalso(Function<Peticion, Respuesta> responder) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> atender(intercambio, responder));
        servidor.start();
    }

    String urlBase() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/countries/v5";
    }

    List<Peticion> recibidas() {
        return recibidas;
    }

    private void atender(HttpExchange intercambio, Function<Peticion, Respuesta> responder) throws IOException {
        Peticion peticion = new Peticion(intercambio.getRequestURI().getPath(),
                parametros(intercambio.getRequestURI().getRawQuery()),
                intercambio.getRequestHeaders().getFirst("Authorization"));
        recibidas.add(peticion);
        Respuesta respuesta = responder.apply(peticion);
        if (respuesta.demoraMs() > 0) {
            try {
                Thread.sleep(respuesta.demoraMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] cuerpo = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(respuesta.codigo(), cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        } catch (IOException e) {
            // El cliente cerró la conexión por tiempo agotado
        }
    }

    private static Map<String, String> parametros(String consulta) {
        Map<String, String> parametros = new HashMap<>();
        if (consulta == null) {
            return parametros;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            String clave = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : URLDecoder.decode(par.substring(igual + 1), StandardCharsets.UTF_8);
            parametros.put(clave, valor);
        }
        return parametros;
    }

    @Override
    public void close() {
        servidor.stop(0);
    }

    // ------------------------------------------------------------------ JSON de ejemplo (forma de la v5)

    /** Objeto país con la forma de la v5. {@code espanol} null = sin traducción al español. */
    static String pais(String comun, String espanol, String capitales, String region, String poblacion,
            String monedas) {
        String traducciones = espanol == null
                ? "{\"fra\":{\"common\":\"" + comun + "\",\"official\":\"" + comun + "\"}}"
                : "{\"spa\":{\"common\":\"" + espanol + "\",\"official\":\"" + espanol + "\"},"
                + "\"fra\":{\"common\":\"" + comun + "\",\"official\":\"" + comun + "\"}}";
        return "{\"names\":{\"common\":\"" + comun + "\",\"official\":\"" + comun + "\",\"translations\":"
                + traducciones + "}," + "\"capitals\":" + capitales + ",\"region\":" + region
                + ",\"population\":" + poblacion + ",\"currencies\":" + monedas + "}";
    }

    static String capital(String nombre, boolean principal) {
        return "{\"name\":\"" + nombre + "\",\"coordinates\":{\"lat\":0,\"lng\":0},"
                + "\"attributes\":{\"primary\":" + principal + ",\"administrative\":false}}";
    }

    static String moneda(String codigo, String nombre) {
        return "{\"code\":\"" + codigo + "\",\"name\":\"" + nombre + "\",\"symbol\":\"$\"}";
    }

    static String pagina(boolean mas, String... paises) {
        return "{\"data\":{\"objects\":[" + String.join(",", paises) + "],\"meta\":{\"total\":0,\"count\":"
                + paises.length + ",\"limit\":100,\"offset\":0,\"more\":" + mas + "}}}";
    }

    static final String ECUADOR = pais("Ecuador", "Ecuador", "[" + capital("Quito", true) + "]",
            "\"Americas\"", "18001000", "[" + moneda("USD", "United States dollar") + "]");
    static final String ALEMANIA = pais("Germany", "Alemania", "[" + capital("Berlin", true) + "]",
            "\"Europe\"", "83491249", "[" + moneda("EUR", "Euro") + "]");
    static final String AUSTRIA = pais("Austria", "Austria", "[" + capital("Vienna", true) + "]",
            "\"Europe\"", "9177982", "[" + moneda("EUR", "Euro") + "]");
}
