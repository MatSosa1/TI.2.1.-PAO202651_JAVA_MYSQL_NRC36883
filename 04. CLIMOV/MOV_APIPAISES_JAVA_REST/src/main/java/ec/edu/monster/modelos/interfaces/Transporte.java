package ec.edu.monster.modelos.interfaces;

import java.io.IOException;
import java.util.Map;

/** Envía una petición HTTP. Las pruebas lo reemplazan por un servidor simulado. */
public interface Transporte {

    /** Código HTTP y cuerpo de la respuesta. */
    final class Respuesta {
        public final int codigo;
        public final String cuerpo;

        public Respuesta(int codigo, String cuerpo) {
            this.codigo = codigo;
            this.cuerpo = cuerpo;
        }
    }

    Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, int esperaMs)
            throws IOException;
}
