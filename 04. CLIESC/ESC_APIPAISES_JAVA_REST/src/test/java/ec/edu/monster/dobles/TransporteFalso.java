package ec.edu.monster.dobles;

import ec.edu.monster.modelo.Transporte;
import java.io.IOException;
import java.net.ConnectException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** Reemplaza la red: guarda la última petición y devuelve la respuesta preparada. */
public class TransporteFalso implements Transporte {

    public String metodo;
    public String url;
    public String cuerpo;
    public Map<String, String> cabeceras = new HashMap<>();
    public Duration espera;

    public int codigo = 200;
    public String respuesta = "";
    public boolean sinRed;

    @Override
    public Respuesta enviar(String metodo, String url, String cuerpo, Map<String, String> cabeceras, Duration espera)
            throws IOException {
        this.metodo = metodo;
        this.url = url;
        this.cuerpo = cuerpo;
        this.cabeceras = cabeceras;
        this.espera = espera;
        if (sinRed) {
            throw new ConnectException("sin red");
        }
        return new Respuesta(codigo, respuesta);
    }
}
