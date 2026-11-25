package ec.edu.monster.controladores;

import ec.edu.monster.modelos.servicios.LoginService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Cerrar sesión (ECUD1 alterno 4). Solo POST: un enlace o una precarga (GET) no deben cerrar la sesión.
 * El token se invalida en el servidor y la sesión local se borra aunque el servidor no responda.
 */
@WebServlet(name = "LogoutController", urlPatterns = {"/logout"})
public class LogoutController extends ControladorBase {

    private final transient LoginService loginService;

    public LogoutController() {
        this(new LoginService());
    }

    LogoutController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = token(request);
        if (token != null) {
            loginService.cerrarSesion(token);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirigir(request, response, RUTA_LOGIN);
    }
}
