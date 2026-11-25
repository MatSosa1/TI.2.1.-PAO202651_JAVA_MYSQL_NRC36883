package ec.edu.monster.controladores;

import ec.edu.monster.modelos.dto.Formatos;
import ec.edu.monster.modelos.servicios.LoginService;
import ec.edu.monster.modelos.servicios.ServicioException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Pantalla de inicio de sesión (ECUD1).
 * GET  /login                  → formulario (con el aviso de sesión vencida si ?sesion=expirada)
 * POST /login usuario, clave   → valida contra el servidor REST y abre el menú
 */
@WebServlet(name = "LoginController", urlPatterns = {"/login"})
public class LoginController extends ControladorBase {

    private static final String VISTA = "login.jsp";
    private static final String RUTA_MENU = "/menu";

    private final transient LoginService loginService;

    public LoginController() {
        this(new LoginService());
    }

    // Lo usan las pruebas para inyectar un modelo con un servidor simulado
    LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("expirada".equals(request.getParameter("sesion"))) {
            request.setAttribute("error", ServicioException.SESION_NO_VALIDA);
        }
        render(request, response, VISTA);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = request.getParameter("usuario");
        String clave = request.getParameter("clave");

        // ECUD1 alterno 2: con un campo vacío no se llama al servidor
        if (usuario == null || usuario.isBlank()) {
            mostrarError(request, response, Formatos.INGRESE_USUARIO, usuario);
            return;
        }
        if (clave == null || clave.isEmpty()) {
            mostrarError(request, response, Formatos.INGRESE_CLAVE, usuario);
            return;
        }

        LoginService.Sesion datos;
        try {
            datos = loginService.iniciarSesion(usuario.trim(), clave);
        } catch (ServicioException e) {
            // ECUD1 alternos 1 y 3: se mantiene el usuario y se borra la clave
            mostrarError(request, response, e.getMessage(), usuario);
            return;
        }

        // Id de sesión nuevo después del login, para no reutilizar uno anterior
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("token", datos.getToken());
        session.setAttribute("usuario", usuario.trim());
        session.setAttribute("nombre", datos.getUsuario());
        // Redirección (no forward): recargar la página no reenvía la clave
        redirigir(request, response, RUTA_MENU);
    }

    private void mostrarError(HttpServletRequest request, HttpServletResponse response, String mensaje,
            String usuario) throws ServletException, IOException {
        request.setAttribute("error", mensaje);
        request.setAttribute("usuario", usuario);
        render(request, response, VISTA);
    }
}
