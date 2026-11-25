package ec.edu.monster.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

/**
 * Configuración del servidor: base de datos y API REST Countries.
 *
 * <p>Cada clave se busca, en este orden, en:
 * <ol>
 *   <li>una propiedad del sistema de Java con el nombre de la clave
 *       (por ejemplo {@code -Drestcountries.apiKey=...} o
 *       {@code asadmin create-system-properties});</li>
 *   <li>una variable de entorno con el nombre en mayúsculas y guiones bajos
 *       (por ejemplo {@code RESTCOUNTRIES_APIKEY});</li>
 *   <li>el archivo {@code config.properties} del classpath (no se versiona, porque
 *       guarda la API key; la plantilla es {@code config.example.properties});</li>
 *   <li>el valor por defecto de esta clase.</li>
 * </ol>
 */
public final class Configuracion {

    public static final String DB_URL = "db.url";
    public static final String DB_USUARIO = "db.usuario";
    public static final String DB_CLAVE = "db.clave";
    public static final String API_URL = "restcountries.url";
    public static final String API_KEY = "restcountries.apiKey";
    public static final String API_TIMEOUT = "restcountries.timeoutSegundos";

    private static final String ARCHIVO = "config.properties";

    private static final Properties POR_DEFECTO = new Properties();

    static {
        // Coinciden con 03. BDD/docker-compose.yml y con la clase Conexion del enunciado.
        // sslMode=DISABLED: dentro de Payara la JVM presenta el certificado del dominio y MySQL
        // lo rechaza (unknown_ca); la base es local. allowPublicKeyRetrieval lo exige
        // caching_sha2_password (MySQL 8.4) cuando no hay SSL.
        POR_DEFECTO.setProperty(DB_URL, "jdbc:mysql://localhost:3306/APIPaisesDB"
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=America/Guayaquil"
                + "&sslMode=DISABLED&allowPublicKeyRetrieval=true");
        POR_DEFECTO.setProperty(DB_USUARIO, "root");
        POR_DEFECTO.setProperty(DB_CLAVE, "12345");
        POR_DEFECTO.setProperty(API_URL, "https://api.restcountries.com/countries/v5");
        POR_DEFECTO.setProperty(API_KEY, "");
        // ECUD: el servidor espera la respuesta de la API pública como máximo 15 segundos
        POR_DEFECTO.setProperty(API_TIMEOUT, "15");
    }

    private static volatile Configuracion instancia;

    private final Properties archivo;
    private final Function<String, String> sistema;
    private final Function<String, String> entorno;

    /** Constructor para pruebas: fuentes explícitas en vez de las del proceso. */
    public Configuracion(Properties archivo, Function<String, String> sistema, Function<String, String> entorno) {
        this.archivo = archivo == null ? new Properties() : archivo;
        this.sistema = sistema;
        this.entorno = entorno;
    }

    /** Configuración del proceso, leída una sola vez. */
    public static Configuracion get() {
        Configuracion actual = instancia;
        if (actual == null) {
            synchronized (Configuracion.class) {
                actual = instancia;
                if (actual == null) {
                    actual = new Configuracion(leerArchivo(), System::getProperty, System::getenv);
                    instancia = actual;
                }
            }
        }
        return actual;
    }

    public String getDbUrl() {
        return valor(DB_URL);
    }

    public String getDbUsuario() {
        return valor(DB_USUARIO);
    }

    public String getDbClave() {
        return valor(DB_CLAVE);
    }

    public String getApiUrl() {
        String url = valor(API_URL);
        // Sin barra final para poder concatenar rutas
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public String getApiKey() {
        return valor(API_KEY);
    }

    public int getApiTimeoutSegundos() {
        try {
            int segundos = Integer.parseInt(valor(API_TIMEOUT));
            return segundos > 0 ? segundos : 15;
        } catch (NumberFormatException e) {
            return 15;
        }
    }

    String valor(String clave) {
        String valor = sistema.apply(clave);
        if (estaVacio(valor)) {
            valor = entorno.apply(clave.toUpperCase(Locale.ROOT).replace('.', '_'));
        }
        if (estaVacio(valor)) {
            valor = archivo.getProperty(clave);
        }
        if (estaVacio(valor)) {
            valor = POR_DEFECTO.getProperty(clave, "");
        }
        return valor.trim();
    }

    private static boolean estaVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static Properties leerArchivo() {
        Properties propiedades = new Properties();
        ClassLoader cargador = Thread.currentThread().getContextClassLoader();
        if (cargador == null) {
            cargador = Configuracion.class.getClassLoader();
        }
        try (InputStream entrada = cargador.getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                propiedades.load(new InputStreamReader(entrada, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + ARCHIVO, e);
        }
        return propiedades;
    }
}
