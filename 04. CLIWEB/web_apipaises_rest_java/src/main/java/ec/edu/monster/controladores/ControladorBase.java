package ec.edu.monster.controladores;

import ec.edu.monster.modelos.servicios.ServicioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Apoyo común de los controladores: token de la sesión, vistas JSP dentro de WEB-INF,
 * mensajes «flash» que sobreviven a una redirección (Post/Redirect/Get) y la vuelta al
 * inicio de sesión cuando el servidor rechaza el token (ECUD8).
 */
abstract class ControladorBase extends HttpServlet {

    static final String VISTAS = "/WEB-INF/ec/edu/monster/vista/";
    static final String RUTA_LOGIN = "/login";
    static final String FLASH_MENSAJE = "flashMensaje";
    static final String FLASH_ERROR = "flashError";

    /** Token de la sesión, o null si no hay usuario (el AuthFilter ya lo exige). */
    static String token(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object token = session.getAttribute("token");
        return token instanceof String valor && !valor.isBlank() ? valor : null;
    }

    static void render(HttpServletRequest request, HttpServletResponse response, String jsp)
            throws ServletException, IOException {
        request.getRequestDispatcher(VISTAS + jsp).forward(request, response);
    }

    /** Mensaje para mostrar después de la próxima redirección. */
    static void flash(HttpServletRequest request, String mensaje, boolean error) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(error ? FLASH_ERROR : FLASH_MENSAJE, mensaje);
        }
    }

    /** Pasa los mensajes flash a la petición (atributos «mensaje» y «error») y los borra. */
    static void leerFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object mensaje = session.getAttribute(FLASH_MENSAJE);
        Object error = session.getAttribute(FLASH_ERROR);
        if (mensaje != null) {
            request.setAttribute("mensaje", mensaje);
            session.removeAttribute(FLASH_MENSAJE);
        }
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute(FLASH_ERROR);
        }
    }

    /**
     * ECUD8 alterno 2: si el servidor rechazó el token, se borra la sesión y se vuelve al login
     * con el aviso. Devuelve true si redirigió.
     */
    static boolean sesionVencida(ServicioException e, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!e.isSesionNoValida()) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + RUTA_LOGIN + "?sesion=expirada");
        return true;
    }

    static void redirigir(HttpServletRequest request, HttpServletResponse response, String ruta) throws IOException {
        response.sendRedirect(request.getContextPath() + ruta);
    }
}
