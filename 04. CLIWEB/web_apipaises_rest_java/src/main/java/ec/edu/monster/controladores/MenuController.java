package ec.edu.monster.controladores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Menú principal (ECUD1 paso 7). El AuthFilter ya comprobó que hay sesión. */
@WebServlet(name = "MenuController", urlPatterns = {"/menu"})
public class MenuController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        render(request, response, "menu.jsp");
    }
}
