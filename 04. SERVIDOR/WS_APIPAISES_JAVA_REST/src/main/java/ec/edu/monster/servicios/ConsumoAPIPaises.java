package ec.edu.monster.servicios;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import ec.edu.monster.config.Configuracion;
import ec.edu.monster.modelos.dto.PaisDTO;
import ec.edu.monster.utilidades.Textos;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Único punto del sistema que conoce la API REST Countries (ECUD9).
 *
 * <p>Usa la versión v5 ({@code https://api.restcountries.com/countries/v5}) con la API key en
 * {@code Authorization: Bearer}, porque la v3.1 del ERS fue dada de baja. Convierte cada país
 * a {@link PaisDTO} con las reglas del ERS §9.1.5:
 * <ul>
 *   <li>nombre: {@code names.translations.spa.common}, o {@code names.common} si no hay traducción;</li>
 *   <li>capital: la capital principal ({@code attributes.primary}), o la primera; «Sin capital» si no tiene;</li>
 *   <li>región: traducida al español; si no es una de las seis, el país se descarta (ECUD9, alterno 4);</li>
 *   <li>población: {@code population} como entero largo;</li>
 *   <li>moneda: la primera, como «nombre (código)»; «Sin moneda» si no tiene.</li>
 * </ul>
 */
public class ConsumoAPIPaises {

    public static final String SIN_CAPITAL = "Sin capital";
    public static final String SIN_MONEDA = "Sin moneda";

    /** Solo los campos que se necesitan (RNF de rendimiento: la importación pide lo mínimo). */
    static final String CAMPOS = "names.common,names.translations,capitals,region,population,currencies";
    /** Tamaño máximo de página del plan gratuito de la v5. */
    static final int LIMITE = 100;
    /** Tope de páginas por si la API nunca dijera que terminó (≈249 países caben en 3). */
    static final int MAX_PAGINAS = 10;

    private static final Logger LOG = Logger.getLogger(ConsumoAPIPaises.class.getName());
    /** El aviso de API key vacía se registra una sola vez (cada petición crea su instancia). */
    private static final AtomicBoolean SIN_KEY_AVISADO = new AtomicBoolean();

    private final String urlBase;
    private final String apiKey;
    private final int timeoutSegundos;
    private final HttpClient http;

    public ConsumoAPIPaises() {
        this(Configuracion.get());
    }

    public ConsumoAPIPaises(Configuracion configuracion) {
        this(configuracion.getApiUrl(), configuracion.getApiKey(), configuracion.getApiTimeoutSegundos());
    }

    public ConsumoAPIPaises(String urlBase, String apiKey, int timeoutSegundos) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.timeoutSegundos = timeoutSegundos;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSegundos))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        if (this.apiKey.isEmpty() && SIN_KEY_AVISADO.compareAndSet(false, true)) {
            LOG.warning("restcountries.apiKey está vacía: la API REST Countries v5 responderá 401");
        }
    }

    /** Todos los países (equivale a la antigua /v3.1/all), recorriendo las páginas de la v5. */
    public List<PaisDTO> obtenerTodos() throws ApiExternaException {
        Map<String, PaisDTO> paises = new LinkedHashMap<>();
        int offset = 0;
        for (int pagina = 0; pagina < MAX_PAGINAS; pagina++) {
            Pagina actual = pedir(urlBase + "?limit=" + LIMITE + "&offset=" + offset + "&response_fields=" + CAMPOS);
            agregarSinRepetir(paises, actual.paises());
            offset += actual.cantidadObjetos();
            if (!actual.hayMas() || actual.cantidadObjetos() == 0) {
                break;
            }
        }
        if (paises.isEmpty()) {
            throw new ApiExternaException(ApiExternaException.Tipo.RESPUESTA_INVALIDA,
                    "La API no devolvió ningún país al pedir la lista completa");
        }
        return new ArrayList<>(paises.values());
    }

    /**
     * Países cuyo nombre contiene el texto (equivale a la antigua /v3.1/name/{nombre}).
     * Primero busca en los nombres en inglés y nativos; si no hay coincidencias, busca en las
     * traducciones y se queda con los países cuyo nombre en español contiene el texto,
     * para que «alemania» encuentre Alemania.
     */
    public List<PaisDTO> obtenerPorNombre(String nombre) throws ApiExternaException {
        String texto = nombre == null ? "" : nombre.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException("El nombre a consultar está vacío");
        }
        Map<String, PaisDTO> paises = new LinkedHashMap<>();
        agregarSinRepetir(paises, pedir(urlBase + "/name" + consulta(texto)).paises());

        if (paises.isEmpty()) {
            agregarSinRepetir(paises, porTraduccion(texto));
        }
        if (paises.isEmpty()) {
            throw new ApiExternaException(ApiExternaException.Tipo.SIN_COINCIDENCIAS,
                    "Sin coincidencias para '" + texto + "'");
        }
        return new ArrayList<>(paises.values());
    }

    /**
     * País elegido por el usuario entre los resultados de su consulta (ECUD2 paso 6, ECUD4
     * Registrar y Actualizar). Si la consulta original no lo trae (por ejemplo, al actualizar
     * un país guardado con su nombre en español), lo busca por ese nombre en las traducciones.
     *
     * @param consulta texto que el usuario consultó; si está vacío se usa el nombre
     * @param nombre   nombre exacto del país elegido
     */
    public PaisDTO obtenerPais(String consulta, String nombre) throws ApiExternaException {
        String texto = Textos.vacio(consulta) ? nombre : consulta;
        PaisDTO elegido;
        try {
            elegido = seleccionar(obtenerPorNombre(texto), nombre);
        } catch (ApiExternaException e) {
            if (e.getTipo() != ApiExternaException.Tipo.SIN_COINCIDENCIAS) {
                throw e;
            }
            elegido = null;
        }
        if (elegido == null) {
            elegido = seleccionar(porTraduccion(nombre.trim()), nombre);
        }
        if (elegido == null) {
            throw new ApiExternaException(ApiExternaException.Tipo.SIN_COINCIDENCIAS,
                    "La API no devolvió el país '" + nombre + "' para la consulta '" + texto + "'");
        }
        return elegido;
    }

    /** Países cuyo nombre en español contiene el texto, buscando en names.translations. */
    private List<PaisDTO> porTraduccion(String texto) throws ApiExternaException {
        String buscado = Textos.clave(texto);
        List<PaisDTO> paises = new ArrayList<>();
        for (PaisDTO pais : pedir(urlBase + "/names.translations" + consulta(texto)).paises()) {
            // names.translations encuentra el texto en cualquier idioma: se filtra por el nombre en español
            if (Textos.clave(pais.getNombre()).contains(buscado)) {
                paises.add(pais);
            }
        }
        return paises;
    }

    private static String consulta(String texto) {
        String q = URLEncoder.encode(texto, StandardCharsets.UTF_8).replace("+", "%20");
        return "?q=" + q + "&limit=" + LIMITE + "&response_fields=" + CAMPOS;
    }

    /**
     * País de la lista cuyo nombre es exactamente el elegido, sin distinguir mayúsculas ni
     * tildes (ECUD9, paso 8: el invocador toma el que coincide). Null si no está.
     */
    public static PaisDTO seleccionar(List<PaisDTO> paises, String nombre) {
        String buscado = Textos.clave(nombre);
        for (PaisDTO pais : paises) {
            if (Textos.clave(pais.getNombre()).equals(buscado)) {
                return pais;
            }
        }
        return null;
    }

    /** Convierte el cuerpo JSON de una respuesta de la v5 en la lista de países. */
    public List<PaisDTO> mapear(String json) throws ApiExternaException {
        return mapearPagina(json).paises();
    }

    /** Traduce la región de la API al nombre del catálogo Regiones; null si no es ninguna de las seis. */
    public static String traducirRegion(String region) {
        if (region == null) {
            return null;
        }
        return switch (region.trim().toLowerCase(Locale.ROOT)) {
            case "americas", "america", "north america", "south america" -> "América";
            case "europe" -> "Europa";
            case "asia" -> "Asia";
            case "africa" -> "África";
            case "oceania" -> "Oceanía";
            case "antarctic", "antarctica", "polar" -> "Antártida";
            default -> null;
        };
    }

    // ------------------------------------------------------------------ HTTP

    private Pagina pedir(String url) throws ApiExternaException {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSegundos))
                .header("Accept", "application/json")
                .header("User-Agent", "APIPaises/1.0")
                .GET();
        if (!apiKey.isEmpty()) {
            peticion.header("Authorization", "Bearer " + apiKey);
        }

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new ApiExternaException(ApiExternaException.Tipo.NO_DISPONIBLE,
                    "Tiempo de espera agotado (" + timeoutSegundos + " s) en " + sinConsulta(url), e);
        } catch (IOException e) {
            throw new ApiExternaException(ApiExternaException.Tipo.NO_DISPONIBLE,
                    "Sin conexión con " + sinConsulta(url) + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiExternaException(ApiExternaException.Tipo.NO_DISPONIBLE, "Petición interrumpida", e);
        }

        int codigo = respuesta.statusCode();
        if (codigo != 200) {
            // 401 (API key), 403 (cuota agotada), 429 (límite de peticiones), 5xx...: la API no está disponible
            String detalle = "REST Countries respondió " + codigo + " en " + sinConsulta(url) + ": "
                    + resumirError(respuesta.body());
            LOG.warning(detalle);
            throw new ApiExternaException(ApiExternaException.Tipo.NO_DISPONIBLE, detalle);
        }
        return mapearPagina(respuesta.body());
    }

    private static String sinConsulta(String url) {
        int i = url.indexOf('?');
        return i < 0 ? url : url.substring(0, i);
    }

    /** Primer mensaje del arreglo errors de la v5, para el log. */
    private static String resumirError(String cuerpo) {
        try {
            JsonObject raiz = JsonParser.parseString(cuerpo).getAsJsonObject();
            JsonArray errores = raiz.getAsJsonArray("errors");
            if (errores != null && !errores.isEmpty()) {
                JsonObject error = errores.get(0).getAsJsonObject();
                String codigo = texto(error, "code");
                return texto(error, "message") + (codigo == null ? "" : " [" + codigo + "]");
            }
        } catch (RuntimeException e) {
            // Cuerpo que no es JSON: se resume tal cual
        }
        String texto = cuerpo == null ? "" : cuerpo.strip();
        return texto.length() <= 200 ? texto : texto.substring(0, 200) + "...";
    }

    // ------------------------------------------------------------------ JSON

    private record Pagina(List<PaisDTO> paises, int cantidadObjetos, boolean hayMas) {
    }

    private Pagina mapearPagina(String json) throws ApiExternaException {
        JsonArray objetos;
        JsonObject meta;
        try {
            JsonElement raiz = JsonParser.parseString(json == null ? "" : json);
            JsonObject datos = raiz.isJsonObject() ? objeto(raiz.getAsJsonObject(), "data") : null;
            JsonElement lista = datos == null ? null : datos.get("objects");
            if (lista == null || !lista.isJsonArray()) {
                throw new ApiExternaException(ApiExternaException.Tipo.RESPUESTA_INVALIDA,
                        "La respuesta no tiene data.objects: " + resumirError(json));
            }
            objetos = lista.getAsJsonArray();
            meta = objeto(datos, "meta");
        } catch (JsonParseException e) {
            throw new ApiExternaException(ApiExternaException.Tipo.RESPUESTA_INVALIDA, "JSON no válido", e);
        }

        List<PaisDTO> paises = new ArrayList<>();
        for (JsonElement elemento : objetos) {
            if (!elemento.isJsonObject()) {
                continue;
            }
            try {
                PaisDTO pais = mapearPais(elemento.getAsJsonObject());
                if (pais != null) {
                    paises.add(pais);
                }
            } catch (RuntimeException e) {
                // Un país con un campo de tipo inesperado no detiene a los demás
                LOG.log(Level.WARNING, "País descartado por formato inesperado: " + elemento, e);
            }
        }

        boolean hayMas = false;
        if (meta != null && meta.get("more") instanceof JsonPrimitive mas && mas.isBoolean()) {
            hayMas = mas.getAsBoolean();
        }
        return new Pagina(paises, objetos.size(), hayMas);
    }

    private PaisDTO mapearPais(JsonObject json) {
        JsonObject nombres = objeto(json, "names");
        String comun = texto(nombres, "common");
        String espanol = texto(objeto(objeto(nombres, "translations"), "spa"), "common");
        String nombre = Textos.vacio(espanol) ? comun : espanol;
        if (Textos.vacio(nombre)) {
            LOG.warning("País descartado: no tiene nombre");
            return null;
        }

        String regionApi = texto(json, "region");
        String region = traducirRegion(regionApi);
        if (region == null) {
            // ECUD9, alterno 4: región que no está en la tabla Regiones
            LOG.warning("País descartado por región desconocida: " + nombre + " (" + regionApi + ")");
            return null;
        }

        return new PaisDTO(Textos.recortar(nombre), Textos.recortar(capital(json.get("capitals"))), region,
                poblacion(json.get("population")), Textos.recortar(moneda(json.get("currencies"))));
    }

    private static String capital(JsonElement capitales) {
        if (capitales == null || !capitales.isJsonArray()) {
            return SIN_CAPITAL;
        }
        String primera = null;
        for (JsonElement elemento : capitales.getAsJsonArray()) {
            String nombre;
            boolean principal = false;
            if (elemento.isJsonObject()) {
                JsonObject capital = elemento.getAsJsonObject();
                nombre = texto(capital, "name");
                JsonObject atributos = objeto(capital, "attributes");
                principal = atributos != null && atributos.get("primary") instanceof JsonPrimitive p
                        && p.isBoolean() && p.getAsBoolean();
            } else if (elemento.isJsonPrimitive()) {
                nombre = elemento.getAsString();
            } else {
                continue;
            }
            if (Textos.vacio(nombre)) {
                continue;
            }
            if (principal) {
                return nombre.trim();
            }
            if (primera == null) {
                primera = nombre.trim();
            }
        }
        return primera == null ? SIN_CAPITAL : primera;
    }

    private static long poblacion(JsonElement valor) {
        if (valor instanceof JsonPrimitive numero && numero.isNumber()) {
            long poblacion = Math.round(numero.getAsDouble());
            return Math.max(0, poblacion);
        }
        return 0;
    }

    private static String moneda(JsonElement monedas) {
        if (monedas == null || monedas.isJsonNull()) {
            return SIN_MONEDA;
        }
        if (monedas.isJsonArray()) {
            // v5: [{ "code": "USD", "name": "United States dollar", "symbol": "$" }]
            for (JsonElement elemento : monedas.getAsJsonArray()) {
                if (elemento.isJsonObject()) {
                    JsonObject moneda = elemento.getAsJsonObject();
                    String texto = formatoMoneda(texto(moneda, "name"), texto(moneda, "code"));
                    if (texto != null) {
                        return texto;
                    }
                }
            }
        } else if (monedas.isJsonObject()) {
            // Forma antigua { "USD": { "name": ... } }, por si la API la devolviera
            for (Map.Entry<String, JsonElement> entrada : monedas.getAsJsonObject().entrySet()) {
                String nombre = entrada.getValue().isJsonObject() ? texto(entrada.getValue().getAsJsonObject(), "name") : null;
                String texto = formatoMoneda(nombre, entrada.getKey());
                if (texto != null) {
                    return texto;
                }
            }
        }
        return SIN_MONEDA;
    }

    private static String formatoMoneda(String nombre, String codigo) {
        boolean sinNombre = Textos.vacio(nombre);
        boolean sinCodigo = Textos.vacio(codigo);
        if (sinNombre && sinCodigo) {
            return null;
        }
        if (sinCodigo) {
            return nombre.trim();
        }
        if (sinNombre) {
            return codigo.trim();
        }
        return nombre.trim() + " (" + codigo.trim() + ")";
    }

    private static JsonObject objeto(JsonObject padre, String clave) {
        if (padre == null) {
            return null;
        }
        JsonElement valor = padre.get(clave);
        return valor != null && valor.isJsonObject() ? valor.getAsJsonObject() : null;
    }

    private static String texto(JsonObject padre, String clave) {
        if (padre == null) {
            return null;
        }
        JsonElement valor = padre.get(clave);
        return valor != null && valor.isJsonPrimitive() ? valor.getAsString() : null;
    }

    private static void agregarSinRepetir(Map<String, PaisDTO> destino, List<PaisDTO> paises) {
        for (PaisDTO pais : paises) {
            destino.putIfAbsent(Textos.clave(pais.getNombre()), pais);
        }
    }
}
