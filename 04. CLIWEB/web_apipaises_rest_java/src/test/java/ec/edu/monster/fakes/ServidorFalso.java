package ec.edu.monster.fakes;

import ec.edu.monster.modelos.servicios.ClienteRest;
import ec.edu.monster.modelos.servicios.Transporte;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor REST de APIPaises simulado: responde por «MÉTODO /ruta» con el JSON preparado y
 * guarda las peticiones recibidas. Sin respuesta preparada devuelve la página 404 de Payara.
 */
public class ServidorFalso implements Transporte {

    public static final String BASE = "http://servidor:8080/WS_APIPAISES_JAVA_REST/api";
    public static final String TOKEN = "t".repeat(64);
    public static final String ECUADOR = "{\"nombre\":\"Ecuador\",\"capital\":\"Quito\",\"region\":\"América\","
            + "\"poblacion\":18001000,\"moneda\":\"United States dollar (USD)\"}";
    public static final String ALEMANIA = "{\"nombre\":\"Alemania\",\"capital\":\"Berlin\",\"region\":\"Europa\","
            + "\"poblacion\":83491249,\"moneda\":\"Euro (EUR)\"}";

    /** Petición recibida. */
    public record Peticion(String metodo, String ruta, String cuerpo, Map<String, String> cabeceras) {
    }

    private final Map<String, Respuesta> respuestas = new HashMap<>();
    public final List<Peticion> peticiones = new ArrayList<>();
    public boolean sinRed;

    public ClienteRest cliente() {
        return new ClienteRest(BASE, this);
    }

    public ServidorFalso ok(String metodoRuta, String mensaje, String datos) {
        return responder(metodoRuta, 200, "OK", mensaje, datos);
    }

    public ServidorFalso error(String metodoRuta, int codigo, String mensaje) {
        return responder(metodoRuta, codigo, "ERROR", mensaje, "null");
    }

    public ServidorFalso responder(String metodoRuta, int codigo, String estado, String mensaje, String datos) {
        respuestas.put(metodoRuta, new Respuesta(codigo,
                "{\"datos\":" + datos + ",\"estado\":\"" + estado + "\",\"mensaje\":\"" + mensaje + "\"}"));
        return this;
    }

    public Peticion ultima() {
        return peticiones.get(peticiones.size() - 1);
    }

    @Override
    public Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
            throws IOException {
        String ruta = url.substring(BASE.length());
        peticiones.add(new Peticion(metodo, ruta, cuerpo, cabeceras));
        if (sinRed) {
            throw new ConnectException("sin red");
        }
        return respuestas.getOrDefault(metodo + " " + ruta, new Respuesta(404, "<html>Not Found</html>"));
    }
}
