package ec.edu.monster.servicios;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Tokens de sesión (ECUD1, ECUD8). El token es el SHA-256 de usuario + hora actual +
 * valor aleatorio, y se guarda en memoria entre los tokens activos. No vence: deja de
 * valer al cerrar sesión o al reiniciar el servidor (decisión del punto de control 1).
 *
 * <p>El registro es estático: todas las instancias (filtro y controladores) comparten
 * los mismos tokens activos.
 */
public class TokenService {

    /** 64 caracteres hexadecimales: el formato de un SHA-256 (ECUD8, paso 2). */
    private static final Pattern FORMATO = Pattern.compile("^[0-9a-fA-F]{64}$");

    private static final Map<String, String> TOKENS_ACTIVOS = new ConcurrentHashMap<>();
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final HashUtil hashUtil;

    public TokenService() {
        this(new HashUtil());
    }

    public TokenService(HashUtil hashUtil) {
        this.hashUtil = hashUtil;
    }

    /** Genera un token nuevo para el usuario y lo registra como activo. */
    public String generarToken(String usuario) {
        byte[] aleatorio = new byte[16];
        ALEATORIO.nextBytes(aleatorio);
        String semilla = usuario + "|" + System.currentTimeMillis() + "|" + System.nanoTime()
                + "|" + HexFormat.of().formatHex(aleatorio);
        String token = hashUtil.sha256(semilla);
        TOKENS_ACTIVOS.put(token, usuario);
        return token;
    }

    /** true si el token tiene el formato esperado y está entre los tokens activos. */
    public boolean esValido(String token) {
        return token != null && FORMATO.matcher(token).matches() && TOKENS_ACTIVOS.containsKey(token);
    }

    /** Usuario dueño del token, o null si no está activo. */
    public String usuarioDe(String token) {
        return token == null ? null : TOKENS_ACTIVOS.get(token);
    }

    /** Cierra la sesión: el token deja de ser válido. */
    public void invalidar(String token) {
        if (token != null) {
            TOKENS_ACTIVOS.remove(token);
        }
    }

    /**
     * Extrae el token del encabezado Authorization. El contrato es «&lt;TOKEN&gt;» (ERS 8.1.4);
     * también se acepta el prefijo «Bearer » por comodidad de las herramientas de prueba.
     */
    public static String extraer(String authorization) {
        if (authorization == null) {
            return null;
        }
        String valor = authorization.trim();
        if (valor.regionMatches(true, 0, "Bearer ", 0, 7)) {
            valor = valor.substring(7).trim();
        }
        return valor.isEmpty() ? null : valor;
    }
}
