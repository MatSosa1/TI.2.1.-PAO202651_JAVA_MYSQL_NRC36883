package ec.edu.monster.modelos.servicios;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

/** Envía una petición HTTP. Las pruebas lo reemplazan por un servidor simulado. */
public interface Transporte {

    /** Código HTTP y cuerpo de la respuesta. */
    record Respuesta(int codigo, String cuerpo) {
    }

    Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
            throws IOException, InterruptedException;
}
