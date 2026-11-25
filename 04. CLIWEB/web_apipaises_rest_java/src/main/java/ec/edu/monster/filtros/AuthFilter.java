package ec.edu.monster.filtros;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Control de acceso a todas las pantallas protegidas, ANTES de que corra el controlador:
 * sin usuario con sesión (sin sesión o sin token) se va a /login. Hay un solo tipo de usuario,
 * así que no hay pantallas restringidas por rol.
 */
@WebFilter(filterName = "AuthFilter",
        urlPatterns = {"/menu", "/paises", "/paises/nuevo", "/consulta", "/busqueda", "/informes"})
public class AuthFilter extends HttpFilter {

    private static final String RUTA_LOGIN = "/login";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // getSession(false): nunca se crea una sesión solo para revisar el inicio de sesión
        HttpSession session = request.getSession(false);
        if (!tieneToken(session)) {
            response.sendRedirect(request.getContextPath() + RUTA_LOGIN);
            return;
        }
        // no-store: después de cerrar sesión, «Atrás» no muestra la página desde la caché
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }

    private static boolean tieneToken(HttpSession session) {
        if (session == null) {
            return false;
        }
        Object token = session.getAttribute("token");
        return token instanceof String valor && !valor.isBlank();
    }
}
