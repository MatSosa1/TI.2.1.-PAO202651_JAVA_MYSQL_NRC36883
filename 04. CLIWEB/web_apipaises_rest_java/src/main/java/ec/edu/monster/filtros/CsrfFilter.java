package ec.edu.monster.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protección CSRF con el patrón «synchronizer token», igual que en el cliente web de EurekaBank.
 * 1. GET (y todo lo que no es POST): la sesión tiene un token para que los formularios lo
 *    incluyan (partials/csrf.jspf). Solo GET /login puede crear la sesión.
 * 2. POST: el parámetro «_csrf» debe ser igual al token de la sesión; si no, se rechaza antes
 *    del controlador (POST /login → formulario nuevo; el resto → 403).
 */
@WebFilter(filterName = "CsrfFilter", urlPatterns = {"/login", "/logout", "/menu", "/paises", "/paises/nuevo",
    "/consulta", "/busqueda", "/informes"})
public class CsrfFilter extends HttpFilter {

    public static final String SESSION_ATTRIBUTE = "csrfToken";
    public static final String PARAMETER = "_csrf";

    private static final int TOKEN_BYTES = 32;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final String RUTA_LOGIN = "/login";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        boolean esLogin = RUTA_LOGIN.equals(request.getServletPath());
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            if (!esValido(request)) {
                if (esLogin) {
                    // Formulario de login vencido o falsificado: se muestra uno nuevo
                    response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                return;
            }
        } else {
            // El login necesita sesión para guardar su token; las demás pantallas solo la reutilizan
            HttpSession session = esLogin ? request.getSession(true) : request.getSession(false);
            if (session != null && tokenActual(session) == null) {
                session.setAttribute(SESSION_ATTRIBUTE, nuevoToken());
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean esValido(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String esperado = tokenActual(session);
        String enviado = request.getParameter(PARAMETER);
        if (esperado == null || enviado == null || enviado.isBlank()) {
            return false;
        }
        // Comparación en tiempo constante
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), enviado.getBytes(StandardCharsets.UTF_8));
    }

    private static String tokenActual(HttpSession session) {
        Object token = session.getAttribute(SESSION_ATTRIBUTE);
        return token instanceof String valor && !valor.isBlank() ? valor : null;
    }

    private static String nuevoToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}
