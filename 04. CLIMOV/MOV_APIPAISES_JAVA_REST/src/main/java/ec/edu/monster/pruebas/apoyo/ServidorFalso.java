package ec.edu.monster.pruebas.apoyo;

import ec.edu.monster.modelos.interfaces.Transporte;
import ec.edu.monster.modelos.interfaces.VerificadorRed;
import ec.edu.monster.modelos.repositorios.ApiPaisesRepositorio;
import ec.edu.monster.modelos.repositorios.ClienteRest;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servidor REST de APIPaises simulado (reemplaza la red): responde por «MÉTODO /ruta» con el JSON
 * preparado y guarda las peticiones. Sin respuesta preparada devuelve la página 404 de Payara.
 */
public class ServidorFalso implements Transporte, VerificadorRed {

    public static final String BASE = "http://10.0.2.2:8080/WS_APIPAISES_JAVA_REST/api";
    public static final String TOKEN = "t".repeat(64);
    public static final String ECUADOR = "{\"nombre\":\"Ecuador\",\"capital\":\"Quito\",\"region\":\"América\","
            + "\"poblacion\":18001000,\"moneda\":\"United States dollar (USD)\"}";

    /** Petición recibida. */
    public static final class Peticion {
        public final String metodo;
        public final String ruta;
        public final String cuerpo;
        public final Map<String, String> cabeceras;
        public final int esperaMs;

        Peticion(String metodo, String ruta, String cuerpo, Map<String, String> cabeceras, int esperaMs) {
            this.metodo = metodo;
            this.ruta = ruta;
            this.cuerpo = cuerpo;
            this.cabeceras = cabeceras;
            this.esperaMs = esperaMs;
        }
    }

    private final Map<String, Respuesta> respuestas = new HashMap<>();
    public final List<Peticion> peticiones = new ArrayList<>();
    public boolean sinRed;
    public boolean servidorApagado;

    public ApiPaisesRepositorio repositorio() {
        return new ApiPaisesRepositorio(new ClienteRest(BASE + "/", this, this));
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
    public boolean hayRed() {
        return !sinRed;
    }

    @Override
    public Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, int esperaMs)
            throws IOException {
        String ruta = url.substring(BASE.length());
        peticiones.add(new Peticion(metodo, ruta, cuerpo, cabeceras, esperaMs));
        if (servidorApagado) {
            throw new ConnectException("Connection refused");
        }
        Respuesta respuesta = respuestas.get(metodo + " " + ruta);
        return respuesta != null ? respuesta : new Respuesta(404, "<html>Not Found</html>");
    }
}
