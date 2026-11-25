package ec.edu.monster.modelo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Dirección del servidor, leída de cliente.properties. Se busca, en este orden: el archivo
 * indicado con -Dcliente.config, el que está junto al jar y el que va dentro del jar.
 */
public final class Configuracion {

    public static final String ARCHIVO = "cliente.properties";
    public static final String CLAVE_RUTA = "cliente.config";

    private final Properties propiedades;
    private final String origen;

    public Configuracion(Properties propiedades) {
        this(propiedades, ARCHIVO);
    }

    private Configuracion(Properties propiedades, String origen) {
        this.propiedades = propiedades;
        this.origen = origen;
    }

    public static Configuracion cargar() {
        return cargar(Path.of("."));
    }

    public static Configuracion cargar(Path directorioTrabajo) {
        String ruta = System.getProperty(CLAVE_RUTA);
        if (ruta != null && !ruta.isBlank()) {
            Path archivo = Path.of(ruta.trim());
            if (!Files.isReadable(archivo)) {
                throw new IllegalStateException("No se encontró el archivo indicado en -D"
                        + CLAVE_RUTA + ": " + archivo.toAbsolutePath());
            }
            return desdeArchivo(archivo);
        }
        Path juntoAlJar = directorioTrabajo.resolve(ARCHIVO);
        if (Files.isReadable(juntoAlJar)) {
            return desdeArchivo(juntoAlJar);
        }
        return desdeClasspath();
    }

    public static Configuracion desdeClasspath() {
        Properties props = new Properties();
        try (InputStream entrada = Configuracion.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new IllegalStateException(ARCHIVO + " no está en el classpath");
            }
            props.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO, e);
        }
        return new Configuracion(props, ARCHIVO);
    }

    public static Configuracion desdeArchivo(Path archivo) {
        Properties props = new Properties();
        try (InputStream entrada = Files.newInputStream(archivo)) {
            props.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + archivo.toAbsolutePath(), e);
        }
        return new Configuracion(props, archivo.toAbsolutePath().toString());
    }

    /** http://host:puerto/contexto/api */
    public String getUrlBase() {
        return "http://" + valor("ws.host") + ":" + getPuerto() + "/" + valor("ws.context") + "/api";
    }

    public int getPuerto() {
        String texto = valor("ws.port");
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("'ws.port' no es un número en " + origen + ": " + texto, e);
        }
    }

    private String valor(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta '" + clave + "' en " + origen);
        }
        return valor.trim();
    }
}
