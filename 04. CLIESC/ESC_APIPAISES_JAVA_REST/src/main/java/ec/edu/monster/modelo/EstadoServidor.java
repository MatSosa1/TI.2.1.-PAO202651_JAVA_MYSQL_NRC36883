package ec.edu.monster.modelo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Comprueba si el servidor responde. Pide GET …/api/Paises sin token: cualquier respuesta HTTP
 * (normalmente 401 «Token inválido») significa que el servicio está en línea; sin respuesta
 * en 2 segundos, que no está disponible.
 */
public class EstadoServidor {

    private static final Duration ESPERA = Duration.ofSeconds(2);

    private final URI destino;
    private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(ESPERA).build();

    public EstadoServidor(String urlBase) {
        this(URI.create(urlBase + "/Paises"));
    }

    public EstadoServidor(URI destino) {
        this.destino = destino;
    }

    public boolean disponible() {
        HttpRequest peticion = HttpRequest.newBuilder(destino).timeout(ESPERA).GET().build();
        try {
            HttpResponse<Void> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.discarding());
            // Una página 404 de Payara significa que el servidor está, pero la aplicación no
            return respuesta.statusCode() != 404 && respuesta.statusCode() < 500;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
