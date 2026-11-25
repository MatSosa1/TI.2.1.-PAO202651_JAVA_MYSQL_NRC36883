package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Lee la ubicación del servidor REST de cliente.properties (classpath) y arma la URL base
 * de los servicios: http://host:puerto/contexto/api.
 */
public final class ApiConfig {

    // Archivo que Maven copia de src/main/resources a WEB-INF/classes
    private static final String ARCHIVO = "cliente.properties";

    private static final Properties PROPIEDADES = cargar();

    private ApiConfig() {
    }

    /** p. ej. http://localhost:8080/WS_APIPAISES_JAVA_REST/api */
    public static String getUrlBase() {
        return "http://" + valor("ws.host") + ":" + valor("ws.port") + "/" + valor("ws.context") + "/api";
    }

    private static String valor(String clave) {
        String valor = PROPIEDADES.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta '" + clave + "' en " + ARCHIVO);
        }
        return valor.trim();
    }

    private static Properties cargar() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ApiConfig.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException(ARCHIVO + " no está en el classpath");
            }
            propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        return propiedades;
    }
}
